package com.elasticsharding.exception;

/**
 * @desc :
 * @author: guanjie
 * @date : 2018/09/17
 */
public class ElasticShardInstanceException extends RuntimeException{

    public ElasticShardInstanceException() {
        super();
    }

    public ElasticShardInstanceException(String message) {
        super(message);
    }

    public ElasticShardInstanceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ElasticShardInstanceException(Throwable cause) {
        super(cause);
    }

    protected ElasticShardInstanceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
