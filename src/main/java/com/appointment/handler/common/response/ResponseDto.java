package com.appointment.handler.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseDto<T> {
    private boolean success;
    private String message;
    private String code;
    private T data;

    public static <T> ResponseDto<T> success(String message, T data) {
        return ResponseDto.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ResponseDto<T> success(String message) {
        return success(message, null);
    }

    public static <T> ResponseDto<T> error(String message, String code, T data) {
        return ResponseDto.<T>builder()
                .success(false)
                .message(message)
                .code(code)
                .data(data)
                .build();
    }

    public static <T> ResponseDto<T> error(String message, String code) {
        return error(message, code, null);
    }
}
