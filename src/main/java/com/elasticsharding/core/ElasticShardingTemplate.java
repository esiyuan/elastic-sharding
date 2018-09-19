package com.elasticsharding.core;

import com.elasticsharding.core.shardstrategy.ShardStrategyIntf;
import com.elasticsharding.util.ExecutorFactory;
import com.google.common.base.Preconditions;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

/**
 * @desc : 弹性分片模版
 * @author: guanjie
 * @date : 2018/09/13
 */
public class ElasticShardingTemplate {

    private ElasticShardingFacade elasticShardingFacade;
    /**
     * 分片总数
     */
    private Integer shardCount;
    /**
     * 分片策略
     */
    private ShardStrategyIntf shardStrategyIntf;

    private ExecutorFactory executorFactory;

    private ElasticShardingTemplate() {
    }


    public static ElasticShardingTemplate newInstance(ElasticShardingFacade elasticShardingFacade, Integer shardCount) {
        return newInstance(elasticShardingFacade, null, shardCount, null);
    }

    public static ElasticShardingTemplate newInstance(ElasticShardingFacade elasticShardingFacade, ShardStrategyIntf shardStrategyIntf, Integer shardCount, ExecutorFactory executorFactory) {
        Preconditions.checkNotNull(shardCount);
        Preconditions.checkNotNull(elasticShardingFacade);
        ElasticShardingTemplate elasticShardingTemplate = new ElasticShardingTemplate();
        elasticShardingTemplate.elasticShardingFacade = elasticShardingFacade;
        elasticShardingTemplate.shardCount = shardCount;
        elasticShardingTemplate.shardStrategyIntf = shardStrategyIntf == null ? ShardStrategyIntf.AVERAGE_SHARD_STRATEGY : shardStrategyIntf;
        elasticShardingTemplate.executorFactory = executorFactory == null ? ExecutorFactory.SIMPLE_EXECUTOR_FACTORY : executorFactory;
        return elasticShardingTemplate;
    }

    /**
     * 初始话组件
     */
    public void start() {
        elasticShardingFacade.addListener();
        elasticShardingFacade.registerToZk();
    }

    /**
     * 选出分片并在当前节点执行
     *
     * @param shardOperator
     */
    public void execute(final ShardOperator shardOperator) {
        elasticShardingFacade.waitUntilElectLeader();
        elasticShardingFacade.waitUntilSharing(shardCount, shardStrategyIntf);

        List<Integer> shardNumbers = elasticShardingFacade.getShardNumbers();
        if (CollectionUtils.isEmpty(shardNumbers)) {
            return;
        }
        for (final Integer shardNumber : shardNumbers) {
            executorFactory.getExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    shardOperator.operate(shardNumber);
                }
            });
        }
        elasticShardingFacade.deleteLocalShardNumbersIfNeed();
    }


    public interface ShardOperator {
        /**
         * 业务系统逻辑
         *
         * @param shardNum 分配的分片号
         */
        void operate(Integer shardNum);
    }
}
