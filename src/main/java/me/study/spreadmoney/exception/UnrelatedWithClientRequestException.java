package me.study.spreadmoney.exception;

/**
 * 직접 정의한 RuntimeException, Client의 요청과 관련 없이 발생하는 예외 (ex. 부가적인 로직, 스케쥴링...)
 */
public class UnrelatedWithClientRequestException extends RuntimeException{
    public UnrelatedWithClientRequestException() {
        super();
    }

    public UnrelatedWithClientRequestException(String message) {
        super(message);
    }

    public UnrelatedWithClientRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnrelatedWithClientRequestException(Throwable cause) {
        super(cause);
    }

    protected UnrelatedWithClientRequestException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
