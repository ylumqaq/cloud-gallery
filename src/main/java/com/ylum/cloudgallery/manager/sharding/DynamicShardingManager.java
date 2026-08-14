package com.ylum.cloudgallery.manager.sharding;

import com.ylum.cloudgallery.common.BusinessException;
import com.ylum.cloudgallery.common.ErrorCode;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * 动态分片管理器：负责「动态建表 + 分片规则运行时刷新」。
 *
 * <p>整体思路（对应「分库分表专项设计文档」）：</p>
 * <ol>
 *   <li>团队/私有空间创建后，先执行 {@code CREATE TABLE picture_{spaceId} LIKE picture} 建物理表；</li>
 *   <li>再扫描 MySQL 中已存在的 {@code picture_*} 物理表，重建 {@code actual-data-nodes}；</li>
 *   <li>通过 ShardingSphere 的 {@link ContextManager} 在运行时刷新分片规则，使新表立即参与路由。</li>
 * </ol>
 *
 * <p>规则刷新顺序遵循「先建表、后刷规则」，避免路由到不存在的表。</p>
 */
@Component
public class DynamicShardingManager {

    /** 逻辑库名称（与 MySQL 业务库一致，同时作为 ShardingSphere 数据源名） */
    public static final String DATABASE_NAME = "cloud_gallery";

    /** 逻辑表名（图片表） */
    public static final String LOGIC_TABLE = "picture";

    /** 分片列：space_id */
    public static final String SHARDING_COLUMN = "space_id";

    /** 分片算法名称（规则内引用） */
    public static final String ALGORITHM_NAME = "picture-sharding";

    /** 自定义分片算法实现类全限定名 */
    public static final String ALGORITHM_CLASS = PictureShardingAlgorithm.class.getName();

    /** ShardingSphere 数据源（用于反射获取 ContextManager 刷新规则） */
    private final DataSource shardingSphereDataSource;

    /** MySQL 底层物理数据源（用于绕过 ShardingSphere 执行 DDL 与信息表扫描） */
    private final DataSource physicalDataSource;

    /**
     * 构造注入两个数据源。
     *
     * @param shardingSphereDataSource ShardingSphere 包装后的主数据源
     * @param physicalDataSource       MySQL 底层物理数据源
     */
    public DynamicShardingManager(
            @Qualifier("shardingSphereDataSource") DataSource shardingSphereDataSource,
            @Qualifier("mysqlDataSource") DataSource physicalDataSource) {
        this.shardingSphereDataSource = shardingSphereDataSource;
        this.physicalDataSource = physicalDataSource;
    }

    /**
     * 为指定空间创建图片分表并刷新分片规则。
     *
     * <p>调用时机：空间创建成功后（业务事务外顺序执行）。</p>
     *
     * @param spaceId 空间 ID（分表后缀）
     */
    public void createTableForSpace(Long spaceId) {
        if (spaceId == null) {
            return;
        }
        // 先建表，保证结构一致（与 picture 表完全一致）
        createPictureTable(spaceId);
        // 再刷新规则，让新建的分表立即参与路由
        refreshShardingRule();
    }

    /**
     * 建物理分表：{@code CREATE TABLE picture_{spaceId} LIKE picture}。
     *
     * <p>使用底层物理数据源执行 DDL，绕过 ShardingSphere（ShardingSphere 无法正确路由 DDL）。</p>
     *
     * @param spaceId 空间 ID
     */
    private void createPictureTable(Long spaceId) {
        String tableName = LOGIC_TABLE + "_" + spaceId;
        String sql = "CREATE TABLE IF NOT EXISTS `" + tableName + "` LIKE `" + LOGIC_TABLE + "`";
        try (Connection connection = physicalDataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建图片分表失败：" + tableName);
        }
    }

    /**
     * 运行时刷新分片规则：重新扫描物理分表并覆盖当前规则。
     */
    private void refreshShardingRule() {
        List<String> tables = scanPictureTables(physicalDataSource);
        String actualDataNodes = buildActualDataNodes(tables);
        ShardingRuleConfiguration ruleConfig = buildRuleConfig(actualDataNodes);

        ContextManager contextManager = getContextManager();
        try {
            contextManager.getMetaDataContextManager()
                    .getDatabaseRuleConfigurationManager()
                    .refresh(DATABASE_NAME, ruleConfig, true);
        } catch (SQLException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "刷新分片规则失败");
        }
    }

    /**
     * 反射获取 ShardingSphere 数据源内部的 {@link ContextManager}。
     *
     * <p>ShardingSphere 5.5.3 的 {@code ShardingSphereDataSource} 未对外暴露 {@code getContextManager()}，
     * 但内部持有 {@code private final ContextManager contextManager} 字段，这里通过反射读取。</p>
     *
     * @return ContextManager 实例
     */
    private ContextManager getContextManager() {
        try {
            Field field = shardingSphereDataSource.getClass().getDeclaredField("contextManager");
            field.setAccessible(true);
            return (ContextManager) field.get(shardingSphereDataSource);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取 ShardingSphere ContextManager 失败");
        }
    }

    // ==================== 以下为共享的静态规则构建工具（供配置类与刷新逻辑复用） ====================

    /**
     * 扫描 MySQL 业务库中已存在的图片物理表，返回表名列表（含公共图库 {@code picture}）。
     *
     * @param physicalDataSource MySQL 底层物理数据源
     * @return 表名列表，例如 {@code [picture, picture_1, picture_2]}
     */
    public static List<String> scanPictureTables(DataSource physicalDataSource) {
        List<String> tables = new ArrayList<>();
        // 公共图库固定使用 picture 表
        tables.add(LOGIC_TABLE);
        String sql = "SELECT table_name FROM information_schema.tables "
                + "WHERE table_schema = DATABASE() AND table_name LIKE 'picture\\_%' ORDER BY table_name";
        try (Connection connection = physicalDataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                tables.add(resultSet.getString("table_name"));
            }
            return tables;
        } catch (SQLException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "扫描图片分表失败");
        }
    }

    /**
     * 根据物理表名集合构建 ShardingSphere 的 {@code actual-data-nodes} 表达式。
     *
     * @param tables 物理表名集合（不含数据源前缀）
     * @return actual-data-nodes，例如 {@code cloud_gallery.picture, cloud_gallery.picture_1}
     */
    public static String buildActualDataNodes(List<String> tables) {
        return tables.stream()
                .map(table -> DATABASE_NAME + "." + table)
                .collect(Collectors.joining(", "));
    }

    /**
     * 构建图片表分片规则配置。
     *
     * <p>使用 {@code CLASS_BASED} 标准分片算法，算法实现类为 {@link PictureShardingAlgorithm}。</p>
     *
     * @param actualDataNodes 物理数据节点表达式
     * @return 分片规则配置
     */
    public static ShardingRuleConfiguration buildRuleConfig(String actualDataNodes) {
        // 图片表规则：逻辑表 picture，物理节点由扫描结果决定
        ShardingTableRuleConfiguration tableRule =
                new ShardingTableRuleConfiguration(LOGIC_TABLE, actualDataNodes);
        tableRule.setTableShardingStrategy(
                new StandardShardingStrategyConfiguration(SHARDING_COLUMN, ALGORITHM_NAME));

        // 自定义分片算法：CLASS_BASED + STANDARD 策略，指向 PictureShardingAlgorithm
        Properties algorithmProps = new Properties();
        algorithmProps.setProperty("strategy", "STANDARD");
        algorithmProps.setProperty("algorithmClassName", ALGORITHM_CLASS);
        AlgorithmConfiguration algorithmConfig =
                new AlgorithmConfiguration("CLASS_BASED", algorithmProps);

        ShardingRuleConfiguration ruleConfig = new ShardingRuleConfiguration();
        ruleConfig.setTables(Collections.singletonList(tableRule));
        ruleConfig.setShardingAlgorithms(Collections.singletonMap(ALGORITHM_NAME, algorithmConfig));
        return ruleConfig;
    }
}
