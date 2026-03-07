package com.duri.duriauth.dto.response;

import com.duri.duriauth.exception.BaseErrorCode;

public record ErrorResponse(
        String code,
        String message
) {

    public static ErrorResponse from (BaseErrorCode errorCode) {
        return new ErrorResponse(
                errorCode.getCode(),
                errorCode.getMessage()
        );
    }
}
