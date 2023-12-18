package me.study.spreadmoney.api;

import me.study.spreadmoney.exception.PredictableRuntimeException;

public class ControllerHelper {
    public static final String SUCCESS_MSG = "요청이 성공적으로 처리되었습니다.";
    public static final String HEADER_USER_ID = "X-USER-ID";
    public static final String HEADER_ROOM_ID = "X-ROOM-ID";

    public static void checkHeaderValue(int userId, String roomId) {
        if (userId <= 0)
            throw new PredictableRuntimeException("Header 의 X-USER-ID 값이 올바르지 않습니다.");
        else if(roomId == null || roomId.isEmpty())
            throw new PredictableRuntimeException("Header 의 X-ROOM-ID 값이 없거나 비어있습니다.");
    }
}
