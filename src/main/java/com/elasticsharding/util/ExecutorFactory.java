package com.elasticsharding.util;

import java.util.concurrent.Executor;

/**
 * @desc : 线程执行器工厂
 * @author: guanjie
 * @date : 2018/09/18
 */
public interface ExecutorFactory {

    ExecutorFactory SIMPLE_EXECUTOR_FACTORY = new SimpleExecutorFactory();

    /**
     * 获取线程执行器
     *
     * @return
     */
    Executor getExecutor();
}
