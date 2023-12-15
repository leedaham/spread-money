package me.study.spreadmoney.api.response;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class FailResult implements Result{
    private HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
    private String message;

    public FailResult(String message) {
        this.message = message;
    }
}
