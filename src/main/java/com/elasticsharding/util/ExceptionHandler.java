package com.elasticsharding.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.KeeperException;

/**
 * @desc : 异常处理
 * @author: guanjie
 * @date : 2018/09/13
 */
@Slf4j
public class ExceptionHandler {


    public static void zkExceptionHandler(Exception e) {
        if (e instanceof KeeperException.NodeExistsException) {
            log.debug("节点存在，忽略异常!");
        } else {
            log.error("", e);
        }
    }
}
