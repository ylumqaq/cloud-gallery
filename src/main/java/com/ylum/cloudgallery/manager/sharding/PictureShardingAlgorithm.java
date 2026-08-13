package com.ylum.cloudgallery.manager.sharding;

import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.Collection;
import java.util.Collections;

/**
 * 图片表自定义分片算法：按 {@code space_id} 精确路由。
 *
 * <p>路由规则（对应「分库分表专项设计文档」）：</p>
 * <ul>
 *   <li>{@code space_id} 为空（公共图库）→ 落到逻辑表 {@code picture}；</li>
 *   <li>{@code space_id} 非空（私有 / 团队空间）→ 落到物理表 {@code picture_{spaceId}}。</li>
 * </ul>
 *
 * <p>说明：分片键类型为 {@link Long}，与实体 {@code Picture.spaceId} 一致；
 * 分表仅需等值精确路由（{@code space_id = ?}），范围分片直接返回空集合。</p>
 */
public class PictureShardingAlgorithm implements StandardShardingAlgorithm<Long> {

    /**
     * 精确分片：根据分片值（space_id）决定落到哪张物理表。
     *
     * @param availableTargetNames 当前逻辑表可用的物理表名集合（由 actual-data-nodes 决定）
     * @param shardingValue        精确分片值，携带分片列名与具体分片值
     * @return 目标物理表名（不带数据源前缀）
     */
    @Override
    public String doSharding(Collection<String> availableTargetNames,
                             PreciseShardingValue<Long> shardingValue) {
        Long spaceId = shardingValue.getValue();
        String logicTable = shardingValue.getLogicTableName();
        // 公共图库（space_id 为空）落到逻辑表 picture，否则落到 picture_{spaceId}
        return spaceId == null ? logicTable : logicTable + "_" + spaceId;
    }

    /**
     * 范围分片：业务当前不涉及按 space_id 范围查询，返回空集合即可。
     *
     * @param availableTargetNames 当前逻辑表可用的物理表名集合
     * @param shardingValue        范围分片值
     * @return 空集合（不支持范围路由）
     */
    @Override
    public Collection<String> doSharding(Collection<String> availableTargetNames,
                                         RangeShardingValue<Long> shardingValue) {
        return Collections.emptyList();
    }
}
