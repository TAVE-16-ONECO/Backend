package com.oneco.backend.mission.domain.exception;

import org.springframework.http.HttpStatus;

import com.oneco.backend.global.exception.BaseException;

public class MissionException extends BaseException {

  private MissionException(String message, HttpStatus httpStatus, String code) {
    super(message, httpStatus, code);
  }

    public static MissionException from(MissionErrorCode errorCode) {
        return new MissionException(errorCode.getMessage(), errorCode.getHttpStatus(), errorCode.getCode());
    }
}
