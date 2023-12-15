package me.study.spreadmoney.api.response;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class SuccessResult implements Result{
    private HttpStatus httpStatus = HttpStatus.OK;
    private String message;
    private Object data;

    public SuccessResult(String message, Object data) {
        this.message = message;
        this.data = data;
    }
}
