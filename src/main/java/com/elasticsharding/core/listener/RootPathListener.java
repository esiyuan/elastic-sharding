package com.elasticsharding.core.listener;

import com.elasticsharding.util.NodePathConstants;
import com.elasticsharding.util.ZookeeperCoordinator;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;

/**
 * @desc :
 * @author: guanjie
 * @date : 2018/09/17
 */
@Slf4j
public class RootPathListener implements TreeCacheListener {

    private ZookeeperCoordinator zookeeperCoordinator;

    public RootPathListener(ZookeeperCoordinator zookeeperCoordinator) {
        this.zookeeperCoordinator = zookeeperCoordinator;
    }

    @Override
    public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
        if (event.getData() == null) {
            return;
        }
        boolean instanceEvent = event.getData().getPath().startsWith(NodePathConstants.INSTANCE_REG_PATH)
                && ((event.getType() == TreeCacheEvent.Type.NODE_REMOVED) || (event.getType() == TreeCacheEvent.Type.NODE_ADDED));
        if (instanceEvent) {
            log.info("{}节点变动，需要重新分片！", event.getData().getPath());
            zookeeperCoordinator.persistNode(NodePathConstants.NEED_SHARD_FLAG_PATH, false);
        }
    }
}
