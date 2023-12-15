package me.study.spreadmoney.api.response;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class ErrorResult implements Result {
    private HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    private String message;

    public ErrorResult(String message) {
        this.message = message;
    }
}
