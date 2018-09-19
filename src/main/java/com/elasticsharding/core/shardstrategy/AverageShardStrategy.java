package com.elasticsharding.core.shardstrategy;

import com.elasticsharding.exception.ElasticShardInstanceException;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;

/**
 * @desc :  平均的分布分片到节点
 * @author: guanjie
 * @date : 2018/09/17
 */
@Slf4j
public class AverageShardStrategy implements ShardStrategyIntf {
    @Override
    public Map<String, List<Integer>> doSharding(List<String> instances, int shardCount) {
        Preconditions.checkState(shardCount > 0);
        if (CollectionUtils.isEmpty(instances)) {
            throw new ElasticShardInstanceException("实例不能为空");
        }
        log.info("需要分片的节点：{}", instances);
        Map<String, List<Integer>> result = new HashMap<>();
        if (instances.size() >= shardCount) {
            for (int i = 0; i < instances.size(); i++) {
                if (i < shardCount) {
                    result.put(instances.get(i), Arrays.asList(i));
                } else {
                    result.put(instances.get(i), Collections.EMPTY_LIST);
                }
            }
        } else {
            for (int i = 0; i < instances.size(); i++) {
                result.put(instances.get(i), new ArrayList<Integer>());
            }
            f1:
            for (int i = 0; i < shardCount; ) {
                for (int j = 0; j < instances.size(); j++) {
                    result.get(instances.get(j)).add(i);
                    if (++i >= shardCount) {
                        break f1;
                    }

                }
            }
        }

        log.info("分片结果:{}", result);
        return result;
    }
}
