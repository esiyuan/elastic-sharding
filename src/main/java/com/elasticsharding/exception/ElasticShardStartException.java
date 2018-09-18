package com.elasticsharding.exception;

/**
 * @desc : elasticSharding启动异常，出现此异常代表程序不能正常执行
 * @author: guanjie
 * @date : 2018/09/13
 */
public class ElasticShardStartException extends RuntimeException {


    public ElasticShardStartException() {
    }

    public ElasticShardStartException(String message) {
        super(message);
    }

    public ElasticShardStartException(String message, Throwable cause) {
        super(message, cause);
    }

    public ElasticShardStartException(Throwable cause) {
        super(cause);
    }

    public ElasticShardStartException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
