package com.elasticsharding.core;

import com.elasticsharding.util.IpOrPidUtil;
import com.elasticsharding.util.NodePathConstants;
import com.elasticsharding.util.ZookeeperCoordinator;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @desc :
 * @author: guanjie
 * @date : 2018/09/13
 */
@Slf4j
public class InstanceService {

    private ZookeeperCoordinator zookeeperCoordinator;

    public InstanceService(ZookeeperCoordinator zookeeperCoordinator) {
        this.zookeeperCoordinator = zookeeperCoordinator;
    }

    /**
     * 获取当前节点的唯一标识
     *
     * @return
     */
    public static String getLocalInstanceId() {
        return IpOrPidUtil.getLocalIp() + "@-@" + IpOrPidUtil.getPid();
    }


    public void registerToZk() {
        zookeeperCoordinator.persistNode(NodePathConstants.INSTANCE_REG_PATH + "/" + getLocalInstanceId(), true);
    }


    public List<String> getInstanceList() {
        return zookeeperCoordinator.getChildrenList(NodePathConstants.INSTANCE_REG_PATH);
    }

}

