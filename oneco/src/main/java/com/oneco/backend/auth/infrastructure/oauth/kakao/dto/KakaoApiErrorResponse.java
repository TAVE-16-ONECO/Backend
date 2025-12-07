package com.oneco.backend.auth.infrastructure.oauth.kakao.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// {
// 	"code": -401,
// 	"msg": "this access token does not exist"
// 	}
@JsonIgnoreProperties(ignoreUnknown = true)
public record KakaoApiErrorResponse (
	Integer code,
	String msg
){
}