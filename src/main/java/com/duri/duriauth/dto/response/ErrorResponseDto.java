package com.duri.duriauth.dto.response;

import com.duri.duriauth.exception.BaseErrorCode;

public record ErrorResponseDto(
        String code,
        String message
) {

    public static ErrorResponseDto from (BaseErrorCode errorCode) {
        return new ErrorResponseDto(
                errorCode.getCode(),
                errorCode.getMessage()
        );
    }
}
