package me.study.spreadmoney.exception;

/**
 * 직접 정의한 RuntimeException, Client의 요청에 의한 로직 수행 중 예상할 수 있는 예외
 */
public class PredictableRuntimeException extends RuntimeException{
    public PredictableRuntimeException() {
        super();
    }

    public PredictableRuntimeException(String message) {
        super(message);
    }

    public PredictableRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public PredictableRuntimeException(Throwable cause) {
        super(cause);
    }

    protected PredictableRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
