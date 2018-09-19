package com.elasticsharding.core;

import com.elasticsharding.core.shardstrategy.AverageShardStrategy;
import com.elasticsharding.core.shardstrategy.ShardStrategyIntf;
import com.elasticsharding.util.NodePathConstants;
import com.elasticsharding.util.ZookeeperCoordinator;
import com.google.common.base.Charsets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * @desc : 分片逻辑业务处理类
 * @author: guanjie
 * @date : 2018/09/17
 */
@Slf4j
public class ShardService {

    private ZookeeperCoordinator zookeeperCoordinator;
    private InstanceService instanceService;

    private volatile List<Integer> localShardNumbers = Collections.EMPTY_LIST;

    public ShardService(ZookeeperCoordinator zookeeperCoordinator, InstanceService instanceService) {
        this.zookeeperCoordinator = zookeeperCoordinator;
        this.instanceService = instanceService;
    }

    public boolean needSharding() {
        return zookeeperCoordinator.existNode(NodePathConstants.NEED_SHARD_FLAG_PATH);
    }

    public boolean canSharding() {
        return CollectionUtils.isEmpty(getShardList());
    }

    /**
     * 获取所有分片节点字符串
     *
     * @return
     */
    private List<String> getShardList() {
        return zookeeperCoordinator.getChildrenList(NodePathConstants.SHARD_NUM_INSTANCE_PATH);
    }

    /**
     * 把分片数分配到实例上，返回实例-分片的列表
     *
     * @param shardCount
     * @param shardStrategyIntf
     * @return Map
     */
    public Map<String, List<Integer>> sharding(int shardCount, ShardStrategyIntf shardStrategyIntf) {
        if (shardStrategyIntf == null) {
            shardStrategyIntf = new AverageShardStrategy();
        }
        return shardStrategyIntf.doSharding(instanceService.getInstanceList(), shardCount);
    }

    /**
     * 分片对应节点
     *
     * @param shardNo
     * @param instanceId
     */
    public void shardInstanceReg(Integer shardNo, String instanceId) {
        zookeeperCoordinator.persistNode(NodePathConstants.SHARD_NUM_INSTANCE_PATH + "/" + shardNo, instanceId.getBytes(Charsets.UTF_8), false);
    }

    /**
     * 删除需要分片的标记
     */
    public void deleteShardFlag() {
        zookeeperCoordinator.delete(NodePathConstants.NEED_SHARD_FLAG_PATH);
    }


    /**
     * 删除已经故障了的节点的分片信息
     */
    public void deleteCrashInstanceShard() {
        List<String> shardList = getShardList();
        if (CollectionUtils.isEmpty(shardList)) {
            return;
        }
        List<String> regInstances = zookeeperCoordinator.getChildrenList(NodePathConstants.INSTANCE_REG_PATH);
        log.info("已注册节点 : {}", regInstances);
        Set<String> regInstanceSet = new HashSet<>(regInstances);
        for (String num : shardList) {
            String data = zookeeperCoordinator.getStringData(NodePathConstants.SHARD_NUM_INSTANCE_PATH + "/" + num);
            if (!regInstanceSet.contains(data)) {
                log.info("删除宕机{}节点的分片信息", data);
                zookeeperCoordinator.delete(NodePathConstants.SHARD_NUM_INSTANCE_PATH + "/" + num);
            }
        }
    }


    /**
     * 获取当前节点的分片信息，如果本地不存在，则取远程
     *
     * @return
     */
    public List<Integer> getShardNumbers() {
        if (CollectionUtils.isNotEmpty(localShardNumbers)) {
            return localShardNumbers;
        }
        log.info("{}获取远程分片信息!", InstanceService.getLocalInstanceId());
        final List<String> shardList = getShardList();
        if (CollectionUtils.isEmpty(shardList)) {
            return Collections.EMPTY_LIST;
        }
        List<Integer> result = new ArrayList<>();

        for (Iterator<String> it = shardList.iterator(); it.hasNext(); ) {
            String shardNo = it.next();
            boolean retained = StringUtils.equals(zookeeperCoordinator.getStringData(NodePathConstants.SHARD_NUM_INSTANCE_PATH + "/" + shardNo),
                    InstanceService.getLocalInstanceId());
            if (retained) {
                result.add(Integer.parseInt(shardNo));
            }
        }
        localShardNumbers = result;
        return result;
    }

    /**
     * 删除本节点的分片信息
     */
    public void deleteLocalShardNumbersIfNeed() {
        if (!needSharding()) {
            return;
        }
        if (CollectionUtils.isEmpty(localShardNumbers)) {
            return;
        }
        for (Integer localShardNumber : localShardNumbers) {
            zookeeperCoordinator.delete(NodePathConstants.SHARD_NUM_INSTANCE_PATH + "/" + localShardNumber);
        }
        localShardNumbers = Collections.EMPTY_LIST;
    }

}
