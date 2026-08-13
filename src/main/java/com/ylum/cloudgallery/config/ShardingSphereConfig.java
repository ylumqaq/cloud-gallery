package com.ylum.cloudgallery.config;

import com.ylum.cloudgallery.manager.sharding.DynamicShardingManager;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.single.config.SingleRuleConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * ShardingSphere 配置：将 MySQL 底层数据源包装为分片数据源，并作为 MyBatis-Plus 的主数据源。
 *
 * <p>规则说明：</p>
 * <ul>
 *   <li>{@code picture} 逻辑表配置按 {@code space_id} 分表；</li>
 *   <li>{@code user} / {@code space} / {@code space_user} 为不分片的单表，通过 single 规则显式声明
 *       （ShardingSphere 5.4+ 不再自动加载单表，必须手动声明）；</li>
 * </ul>
 *
 * <p>初始化时扫描 MySQL 中已存在的 {@code picture_*} 物理表，构建初始 {@code actual-data-nodes}；
 * 后续空间创建时由 {@link DynamicShardingManager} 动态建表并运行时刷新分片规则。</p>
 */
@Configuration
public class ShardingSphereConfig {

    /**
     * 创建 ShardingSphere 分片数据源（主数据源）。
     *
     * @param mysqlDataSource MySQL 底层物理数据源
     * @return ShardingSphere 包装后的数据源
     */
    @Bean("shardingSphereDataSource")
    @Primary
    public DataSource shardingSphereDataSource(
            @Qualifier("mysqlDataSource") DataSource mysqlDataSource) throws SQLException {
        // 扫描已存在的物理分表，构建初始 actual-data-nodes 与分片规则
        List<String> tables = DynamicShardingManager.scanPictureTables(mysqlDataSource);
        String actualDataNodes = DynamicShardingManager.buildActualDataNodes(tables);
        ShardingRuleConfiguration ruleConfig = DynamicShardingManager.buildRuleConfig(actualDataNodes);

        // 单表规则：user / space / space_user 不分片，需显式声明（ShardingSphere 5.4+ 不再自动加载单表）
        SingleRuleConfiguration singleRuleConfig = new SingleRuleConfiguration();
        singleRuleConfig.setTables(Arrays.asList(
                DynamicShardingManager.DATABASE_NAME + ".user",
                DynamicShardingManager.DATABASE_NAME + ".space",
                DynamicShardingManager.DATABASE_NAME + ".space_user"));

        Collection<RuleConfiguration> rules = Arrays.asList(ruleConfig, singleRuleConfig);
        // mode 传 null 使用默认 Standalone（内存）模式
        return ShardingSphereDataSourceFactory.createDataSource(
                DynamicShardingManager.DATABASE_NAME,
                null,
                mysqlDataSource,
                rules,
                new Properties());
    }
}
