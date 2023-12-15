package me.study.spreadmoney.api;

import lombok.extern.slf4j.Slf4j;
import me.study.spreadmoney.api.response.ErrorResult;
import me.study.spreadmoney.api.response.FailResult;
import me.study.spreadmoney.api.response.Result;
import me.study.spreadmoney.exception.CustomRuntimeException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class RestControllerExceptionHandler {

    @ExceptionHandler(CustomRuntimeException.class)
    public ResponseEntity<Result> handleCustomException(CustomRuntimeException ex) {
        String message = ex.getMessage();
        log.warn(message);
        return ResponseEntity.ok(new FailResult(message));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result> handleValidationException(MethodArgumentNotValidException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "Validation failed";
        log.warn(message);
        return ResponseEntity.ok(new FailResult(message));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<Result> handleHeaderException(MissingRequestHeaderException ex) {
        String message = ex.getMessage();
        log.warn(message);
        return ResponseEntity.ok(new FailResult(message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result> handleNotReadableException(Exception ex) {
        String message = ex.getMessage();
        log.warn(message);
        return ResponseEntity.ok(new FailResult(message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result> handleException(Exception ex) {
        String message = ex.getMessage();
        log.error(message);
        return ResponseEntity.ok(new ErrorResult(message));
    }

}
