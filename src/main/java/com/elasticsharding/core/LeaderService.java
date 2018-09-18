package com.elasticsharding.core;

import com.elasticsharding.util.NodePathConstants;
import com.elasticsharding.util.ZookeeperCoordinator;
import com.google.common.base.Charsets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * @desc :
 * @author: guanjie
 * @date : 2018/09/13
 */
@Slf4j
public class LeaderService {

    private ZookeeperCoordinator zookeeperCoordinator;

    public LeaderService(ZookeeperCoordinator zookeeperCoordinator) {
        this.zookeeperCoordinator = zookeeperCoordinator;
    }

    public boolean existLeader() {
        return zookeeperCoordinator.existNode(NodePathConstants.LEADER_INSTANCE_PATH);
    }


    public void electLeader() {
        zookeeperCoordinator.electLeader(NodePathConstants.LEADER_ELECT_LOCK_PATH,
                new ZookeeperCoordinator.ElectCallBack() {
                    @Override
                    public void callBack() {
                        if (!existLeader()) {
                            zookeeperCoordinator.persistNode(NodePathConstants.LEADER_INSTANCE_PATH,
                                    InstanceService.getLocalInstanceId().getBytes(Charsets.UTF_8), true);
                        }
                    }
                });
    }

    /**
     * 判断当前节点是否leader
     *
     * @return
     */
    public boolean localIsLeader() {
        if (!existLeader()) {
            return false;
        }
        return StringUtils.equals(getLeaderId(), InstanceService.getLocalInstanceId());
    }

    private String getLeaderId() {
        return new String(zookeeperCoordinator.getData(NodePathConstants.LEADER_INSTANCE_PATH), Charsets.UTF_8);
    }

}
