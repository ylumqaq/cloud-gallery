package com.ylum.cloudgallery.manager.sharding;

import org.apache.shardingsphere.infra.datanode.DataNodeInfo;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 图片表分片算法单元测试：验证 {@code space_id} 精确路由规则。
 */
class PictureShardingAlgorithmTest {

    private final PictureShardingAlgorithm algorithm = new PictureShardingAlgorithm();

    /** 构造测试用的 DataNodeInfo（算法 doSharding 不使用该信息，仅用于构造分片值） */
    private static final DataNodeInfo DATA_NODE_INFO = new DataNodeInfo("picture", 1, '0');

    @Test
    void 公共图库_空spaceId_路由到picture() {
        PreciseShardingValue<Long> value =
                new PreciseShardingValue<>("picture", "space_id", DATA_NODE_INFO, null);

        String target = algorithm.doSharding(Arrays.asList("picture"), value);

        assertEquals("picture", target);
    }

    @Test
    void 空间图片_非空spaceId_路由到picture_spaceId() {
        PreciseShardingValue<Long> value =
                new PreciseShardingValue<>("picture", "space_id", DATA_NODE_INFO, 123L);

        String target = algorithm.doSharding(Arrays.asList("picture", "picture_123"), value);

        assertEquals("picture_123", target);
    }

    @Test
    void 范围分片_返回空集合() {
        Collection<String> result = algorithm.doSharding(Arrays.asList("picture", "picture_123"),
                (RangeShardingValue<Long>) null);

        assertTrue(result.isEmpty());
    }
}
