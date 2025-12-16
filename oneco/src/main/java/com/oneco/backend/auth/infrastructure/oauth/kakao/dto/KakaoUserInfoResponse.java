package com.oneco.backend.auth.infrastructure.oauth.kakao.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 응답 json 예
 * {
 * "id":1399634384,
 * "connected_at":"2020-07-06T09:55:51Z",
 * "kakao_account":{
 * "profile_needs_agreement":false,
 * "profile":{
 * "nickname":"춘식이",
 * "thumbnail_image_url":"http://k.kakaocdn.net/dn/zK7QA/btqzpE4aqO1/pl2HpfVBUI9s1SSrstperq/img_110x110.jpg",
 * "profile_image_url":"http://k.kakaocdn.net/dn/zK7QA/btqzpE4aqO1/pl2HpfVBUI9s1wqsgrEAVk/img_640x640.jpg",
 * "is_default_image":false
 * },
 * "email_needs_agreement":false,
 * "is_email_valid":true,
 * "is_email_verified":true,
 * "email":"sweetpotato@kakao.com"
 * }
 * },
 */
@Getter
@NoArgsConstructor
@ToString
public class KakaoUserInfoResponse {

	// 최상위 JSON의 "id" 필드
	private Long id;

	// 최상위 JSON의 "kakao_account" 객체를 통째로 매핑
	//kakaoAccount Jackson 흐름:
	//   │    (1) KakaoUserInfoResponse 인스턴스를 하나 만든다
	//   │    (2) JSON의 "id" → this.id
	//   │    (3) JSON의 "kakao_account" → KakaoAccount 객체 생성
	//   │        그 안의 필드들을 다시 아래 KakaoAccount 클래스로
	//   │        내려가면서 채운다
	@JsonProperty("kakao_account")
	private KakaoAccount kakaoAccount;

	public String getEmail() {
		return kakaoAccount != null ? kakaoAccount.email : null;
	}

	public String getNickname() {
		return (kakaoAccount != null && kakaoAccount.profile != null)
			? kakaoAccount.profile.nickname
			: null;
	}

	public String getProfileImageUrl() {
		return (kakaoAccount != null && kakaoAccount.profile != null)
			? kakaoAccount.profile.profileImageUrl
			: null;
	}

	// JSON: "kakao_account": { "profile": { ... } }
	// → profile 객체 전체를 Profile 타입으로 매핑
	//
	// Jackson 흐름:
	//   (1) "kakao_account" 블록을 읽다가 "profile" 키를 만나면
	//   (2) Profile 인스턴스를 하나 만들고,
	//   (3) 그 안에 있는 nickname, profile_image_url 을 아래 Profile 클래스에 채운다.
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static record KakaoAccount(
		String email,
		Profile profile
	) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static record Profile(
		String nickname,
		@JsonProperty("profile_image_url")
		String profileImageUrl
	) {
	}

}
