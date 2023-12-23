package me.study.spreadmoney.api;

import lombok.extern.slf4j.Slf4j;
import me.study.spreadmoney.api.response.ErrorResult;
import me.study.spreadmoney.api.response.FailResult;
import me.study.spreadmoney.api.response.Result;
import me.study.spreadmoney.exception.PredictableRuntimeException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 예외 발생 처리
 */
@Slf4j
@RestControllerAdvice
public class ControllerExceptionHandler {

    /**
     * HTTP Method 잘못된 경우 발생하는 Exception
     * @param ex HttpRequestMethodNotSupportedException
     * @return 실패 응답 (HttpStatus.BAD_REQUEST)
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Result> handleMethodNotSupportException(HttpRequestMethodNotSupportedException ex) {
        String message = ex.getMessage();
        log.warn(message);
        return ResponseEntity.badRequest().body(new FailResult(message));
    }

    /**
     * 직접 정의한 Exception, 예상 가능한 예외 처리에 사용
     * @param ex 직접 정의한 Exception
     * @return 실패 응답 (HttpStatus.BAD_REQUEST)
     */
    @ExceptionHandler(PredictableRuntimeException.class)
    public ResponseEntity<Result> handleCustomException(PredictableRuntimeException ex) {
        String message = ex.getMessage();
        log.warn(message);
        return ResponseEntity.badRequest().body(new FailResult(message));
    }

    /**
     * Valid 어노테이션에서 검증에 실패할 경우 발생하는 Exception
     * @param ex MethodArgumentNotValidException
     * @return 실패 응답 (HttpStatus.BAD_REQUEST)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result> handleValidationException(MethodArgumentNotValidException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "검증 실패";
        log.warn(message);
        return ResponseEntity.badRequest().body(new FailResult(message));
    }

    /**
     * RequestHeader 어노테이션에서 발생하는 Exception
     * @param ex MissingRequestHeaderException
     * @return 실패 응답 (HttpStatus.BAD_REQUEST)
     */
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<Result> handleHeaderException(MissingRequestHeaderException ex) {
        String message = ex.getMessage();
        log.warn(message);
        return ResponseEntity.badRequest().body(new FailResult(message));
    }

    /**
     * 잘못된 body, parameter 요청시 발생하는 Exception
     * @param ex HttpMessageNotReadableException
     * @return 실패 응답 (HttpStatus.BAD_REQUEST)
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result> handleNotReadableException(HttpMessageNotReadableException ex) {
        String message = ex.getMessage();
        log.warn(message);
        return ResponseEntity.badRequest().body(new FailResult(message));
    }

    /**
     * 앞에서 걸려진 예외 외의 예외로 예상하지 못한 Exception
     * @param ex Exception
     * @return 오류 응답 (HttpStatus.INTERNAL_SERVER_ERROR)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result> handleException(Exception ex) {
        String message = ex.getMessage();
        log.error(message);
        return ResponseEntity.internalServerError().body(new ErrorResult(message));
    }

}
