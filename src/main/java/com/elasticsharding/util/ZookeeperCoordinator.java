package com.elasticsharding.util;

import com.elasticsharding.exception.ElasticShardStartException;
import com.google.common.base.Charsets;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.zookeeper.CreateMode;

import java.util.Collections;
import java.util.List;

/**
 * @desc : zookeeper操作控制类
 * @author: guanjie
 * @date : 2018/09/05
 */
@Slf4j
@Getter
public class ZookeeperCoordinator {


    private CuratorFramework client;

    private ZookeeperCoordinator(CuratorFramework client) {
        this.client = client;
        client.start();
    }

    public static class Builder {
        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder();

        public Builder connectString(String connectString) {
            builder.connectString(connectString);
            return this;
        }

        public Builder namespace(String namespace) {
            builder.namespace(namespace);
            return this;
        }

        public Builder sessionTimeoutMs(int sessionTimeoutMs) {
            builder.sessionTimeoutMs(sessionTimeoutMs);
            return this;
        }

        public Builder connectionTimeoutMs(int connectionTimeoutMs) {
            builder.connectionTimeoutMs(connectionTimeoutMs);
            return this;
        }

        public Builder retryPolicy(RetryPolicy retryPolicy) {
            builder.retryPolicy(retryPolicy);
            return this;
        }

        public ZookeeperCoordinator build() {
            return new ZookeeperCoordinator(builder.build());
        }

    }


    public void persistNode(String path, boolean ephemeral) {
        persistNode(path, ArrayUtils.EMPTY_BYTE_ARRAY, ephemeral);
    }

    public void persistNode(String path, byte[] data, boolean ephemeral) {
        try {
            log.info("persistNode path = {} data = {}", path, new String(data, Charsets.UTF_8));
            if (existNode(path)) {
                updateNode(path, data);
            } else {
                if (ephemeral) {
                    client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path, data);
                } else {
                    client.create().creatingParentsIfNeeded().forPath(path, data);
                }
            }
        } catch (Exception e) {
            ExceptionHandler.zkExceptionHandler(e);
        }
    }

    /**
     * 更新节点数据
     *
     * @param path
     * @param data
     */
    private void updateNode(String path, byte[] data) throws Exception {
        client.setData().forPath(path, data);
    }


    public List<String> getChildrenList(String path) {
        try {
            return client.getChildren().forPath(path);
        } catch (Exception e) {
            ExceptionHandler.zkExceptionHandler(e);
        }
        return Collections.emptyList();
    }

    public byte[] getData(String path) {
        try {
            return client.getData().forPath(path);
        } catch (Exception e) {
            ExceptionHandler.zkExceptionHandler(e);
        }
        return null;
    }

    public String getStringData(String path) {
        try {
            byte[] data = client.getData().forPath(path);
            if (ArrayUtils.isEmpty(data)) {
                return StringUtils.EMPTY;
            }
            return new String(data, Charsets.UTF_8);
        } catch (Exception e) {
            ExceptionHandler.zkExceptionHandler(e);
        }
        return null;
    }


    public boolean existNode(String path) {
        try {
            return client.checkExists().forPath(path) != null;
        } catch (Exception e) {
            ExceptionHandler.zkExceptionHandler(e);
        }
        return false;
    }


    public void delete(String path) {
        try {
            client.delete().deletingChildrenIfNeeded().forPath(path);
        } catch (Exception e) {
            ExceptionHandler.zkExceptionHandler(e);
        }
    }

    /**
     * 监听节点
     *
     * @param path
     * @param treeCacheListener
     */
    public void listenPath(String path, TreeCacheListener treeCacheListener) {
        TreeCache treeCache = new TreeCache(client, path);
        treeCache.getListenable().addListener(treeCacheListener);
        try {
            treeCache.start();
        } catch (Exception e) {
            throw new ElasticShardStartException(e);
        }
    }


    public void electLeader(String path, ElectCallBack electCallBack) {
        try (LeaderLatch latch = new LeaderLatch(client, path)) {
            latch.start();
            latch.await();
            electCallBack.callBack();
        } catch (Exception e) {
            ExceptionHandler.zkExceptionHandler(e);
        }
    }


    /**
     * 选举回调
     */
    public interface ElectCallBack {
        /**
         * 回调方法
         */
        void callBack();
    }

}
