package com.elasticsharding.util;

/**
 * @desc : zookeeper节点常量
 * @author: guanjie
 * @date : 2018/09/13
 */
public class NodePathConstants {

    /**
     * 根节点
     */
    public static final String ROOT_PATH = "/elasticSharing";
    /**
     * 集群节点注册路径
     */
    public static final String INSTANCE_REG_PATH = "/elasticSharing/instance";
    /**
     * leader选举的节点路径
     */
    public static final String LEADER_ELECT_LOCK_PATH = "/elasticSharing/leader/electLock";
    /**
     * leader保存路径
     */
    public static final String LEADER_INSTANCE_PATH = "/elasticSharing/leader/instance";
    /**
     * 需要重新分配分片的标记
     */
    public static final String NEED_SHARD_FLAG_PATH = "/elasticSharing/sharding/needSharding";

    /**
     * 分片序号与对应机器保存路径
     */
    public static final String SHARD_NUM_INSTANCE_PATH = "/elasticSharing/sharding/seq";
}
