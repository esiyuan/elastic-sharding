package com.elasticsharding.util;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @desc :
 * @author: guanjie
 * @date : 2018/09/13
 */
public class SimpleExecutorFactory implements ExecutorFactory {
    private static ExecutorService executor = Executors.newFixedThreadPool(Math.max(Runtime.getRuntime().availableProcessors() * 2, 4));

    @Override
    public Executor getExecutor() {
        return executor;
    }
}
