package com.elasticsharding.core;

import com.elasticsharding.core.listener.RootPathListener;
import com.elasticsharding.core.shardstrategy.ShardStrategyIntf;
import com.elasticsharding.exception.ElasticShardInstanceException;
import com.elasticsharding.util.NodePathConstants;
import com.elasticsharding.util.ZookeeperCoordinator;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @desc : 处理分配分片号逻辑的控制类
 * @author: guanjie
 * @date : 2018/09/13
 */
@Slf4j
public class ElasticShardingFacade {

    /**
     * 循环睡眠时间
     */
    private final static int SLEEP_MS = 200;

    private ZookeeperCoordinator zookeeperCoordinator;

    private InstanceService instanceService;

    private LeaderService leaderService;

    private ShardService shardService;


    public ElasticShardingFacade(ZookeeperCoordinator zookeeperCoordinator) {
        this.zookeeperCoordinator = zookeeperCoordinator;
        this.instanceService = new InstanceService(zookeeperCoordinator);
        this.leaderService = new LeaderService(zookeeperCoordinator);
        this.shardService = new ShardService(zookeeperCoordinator, instanceService);
    }

    /**
     * 获取当前节点的分片信息，如果本地不存在，则取远程
     *
     * @return
     */
    public List<Integer> getShardNumbers() {
        return shardService.getShardNumbers();
    }

    /**
     * 等待直到选出leader，如果有leader则不需要等待
     */
    public void waitUntilElectLeader() {
        if (!leaderService.existLeader()) {
            leaderService.electLeader();
        }
    }

    /**
     * 等待分片完成
     */
    public void waitUntilSharing(int count, ShardStrategyIntf shardStrategyIntf) {
        try {
            while (shardService.needSharding() && !leaderService.localIsLeader()) {
                deleteLocalShardNumbersIfNeed();
                TimeUnit.MILLISECONDS.sleep(SLEEP_MS);
                log.debug("等待leader分片完成！");
            }
            if (!shardService.needSharding() || !leaderService.localIsLeader()) {
                return;
            }
            while (!shardService.canSharding()) {
                deleteLocalShardNumbersIfNeed();
                shardService.deleteCrashInstanceShard();
                TimeUnit.MILLISECONDS.sleep(SLEEP_MS);
                log.debug("等待所有节点执行完成！");
            }
        } catch (InterruptedException e) {
            log.error("", e);
            throw new ElasticShardInstanceException(e);
        }
        Map<String, List<Integer>> listMap = shardService.sharding(count, shardStrategyIntf);
        for (Map.Entry<String, List<Integer>> entry : listMap.entrySet()) {
            for (Integer shardNo : entry.getValue()) {
                shardService.shardInstanceReg(shardNo, entry.getKey());
            }
        }
        shardService.deleteShardFlag();
    }


    public void registerToZk() {
        instanceService.registerToZk();
    }

    /**
     * 删除本节点的分片信息
     */
    public void deleteLocalShardNumbersIfNeed() {
        shardService.deleteLocalShardNumbersIfNeed();
    }

    public void addListener() {
        zookeeperCoordinator.listenPath(NodePathConstants.ROOT_PATH, new RootPathListener(zookeeperCoordinator));
    }
}
