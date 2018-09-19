package com.elasticsharding.core.shardstrategy;

import java.util.List;
import java.util.Map;

/**
 * @desc : 分片策略
 * @author: guanjie
 * @date : 2018/09/17
 */
public interface ShardStrategyIntf {

    ShardStrategyIntf AVERAGE_SHARD_STRATEGY = new AverageShardStrategy();

    /**
     * 把分片数分配到实例上，返回实例-分片的列表
     *
     * @param instances
     * @param ShardCount
     * @return
     */
    Map<String, List<Integer>> doSharding(List<String> instances, int ShardCount);
}
