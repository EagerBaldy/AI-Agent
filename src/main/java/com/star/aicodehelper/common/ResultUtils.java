package com.star.aicodehelper.common;

public class ResultUtils {
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0, data, "ok");
    }

    public static BaseResponse error(int code, String message) {
        return new BaseResponse(code, message);
    }
}
