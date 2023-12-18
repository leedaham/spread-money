package me.study.spreadmoney.api.response;

import lombok.Data;
import org.springframework.http.HttpStatus;

/**
 * 오류 응답 객체
 */
@Data
public class ErrorResult implements Result {
    private HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    private String message;

    public ErrorResult(String message) {
        this.message = message;
    }
}
