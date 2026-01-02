package com.oneco.backend.auth.presentation;

import static com.oneco.backend.global.security.jwt.filter.RefreshTokenFilter.*;

import java.net.URI;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.oneco.backend.auth.application.AuthService;
import com.oneco.backend.auth.application.TokenReissueService;
import com.oneco.backend.auth.application.bridge.LoginBridgeStore;
import com.oneco.backend.auth.application.dto.KakaoLoginResponse;
import com.oneco.backend.auth.application.dto.TokenReissueResponse;
import com.oneco.backend.auth.infrastructure.oauth.kakao.config.KakaoOAuthProperties;
import com.oneco.backend.global.config.FrontendProperties;
import com.oneco.backend.global.exception.BaseException;
import com.oneco.backend.global.exception.constant.GlobalErrorCode;
import com.oneco.backend.global.response.DataResponse;

import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

	private final KakaoOAuthProperties kakaoOAuthProperties;
	private final AuthService authService;
	private static final String KAKAO_OAUTH_STATE_SESSION_KEY = "KAKAO_OAUTH_STATE";
	private final TokenReissueService tokenReissueService;
	private final FrontendProperties frontendProperties;
	private final LoginBridgeStore loginBridgeStore;

	@Operation(
		summary = "카카오 로그인 시작(Authorize로 리다이렉트)",
		description = """
			카카오 OAuth 인가 페이지로 302 리다이렉트한다.
			서버에서 state를 생성해 세션(또는 Redis)에 저장한 뒤 authorize URL에 포함한다.
			"""
	)
	@ApiResponses({
		@ApiResponse(responseCode = "302", description = "카카오 인가 페이지로 리다이렉트"),
		@ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
	})
	@GetMapping("/kakao/login")
	public ResponseEntity<Void> redirectTokKakao(
		@Parameter(hidden = true) HttpSession session,
		@Parameter(hidden = true) HttpServletRequest request) {
		String redirectUri = kakaoOAuthProperties.getRedirectUri();

		// state 생성 + 서버에 저장 (세션 or Redis)
		String state = UUID.randomUUID().toString();
		log.info("로그인 요청: uri={}, query={}, sessionId={}, thread={}, state={}",
			request.getRequestURI(),
			request.getQueryString(),
			session.getId(),
			Thread.currentThread().getName(),
			state
		);
		//log.info("생성한 state {}",state);
		// TODO: 추후 레디스 or jwt로 state 검증
		session.setAttribute(KAKAO_OAUTH_STATE_SESSION_KEY, state);

		// scope 구성
		String scope = "openid profile_nickname profile_image";
		// 카카오 authorize URL 만들기
		// URI는 불변 객체 (한 번 만들면 내부 값이 바뀌지 않음)
		// -> 최종적으로 만들어진 URL/주소를 담는 그릇
		URI kakaoAuthorizeUri = UriComponentsBuilder
			.fromUriString(kakaoOAuthProperties.getBaseUrl()) //https://kauth.kakao.com
			.path("/oauth/authorize")
			.queryParam("response_type", "code")
			.queryParam("client_id", kakaoOAuthProperties.getClientId())
			.queryParam("redirect_uri", redirectUri)
			.queryParam("scope", scope)
			.queryParam("state", state)
			.build()
			.encode() // redirectUri에 특수문자/한글 들어가도 안전하게
			.toUri();

		HttpHeaders headers = new HttpHeaders();
		headers.setLocation(kakaoAuthorizeUri);

		return ResponseEntity.status(HttpStatus.FOUND)
			.headers(headers)
			.build();
	}

	@Operation(
		summary = "카카오 로그인 콜백"
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "로그인 처리 결과 반환"),
		@ApiResponse(responseCode = "400", description = "state 불일치/요청 오류", content = @Content)
	})
	@GetMapping("/kakao/callback")
	public ResponseEntity<Void> kakaoCallback(
		@Parameter(description = "카카오 인가 코드", required = true)
		@RequestParam("code") String code,
		@Parameter(description = "CSRF 방지용 state", required = true)
		@RequestParam("state") String state,
		@Parameter(hidden = true) HttpSession session
	) {
		String expectedState = (String)session.getAttribute(KAKAO_OAUTH_STATE_SESSION_KEY);
		log.info("받은 state: {}", state);
		log.info("생성한 state: {}", expectedState);
		session.removeAttribute(KAKAO_OAUTH_STATE_SESSION_KEY);
		KakaoLoginResponse response = authService.loginWithKakao(code, state, expectedState);

		// ✅ 로그인 결과를 브릿지 저장소에 30~60초 단위로 저장하고 key 발급
		String key = loginBridgeStore.save(response);
		String frontendUri = frontendProperties.getBaseUrl();
		log.info("frontendProperties: {}", frontendUri);
		URI redirect = UriComponentsBuilder
			.fromUriString(frontendProperties.getBaseUrl())
			.path("/login-bridge")
			.queryParam("key", key)
			.build()
			.toUri();

		log.info("카카오 로그인 콜백 리다이렉트: {}", redirect.toString());
		return ResponseEntity.status(HttpStatus.FOUND)
			.location(redirect)
			.build();

	}

	/**
	 * 3) 브릿지 결과 조회
	 * - 프론트가 key로 로그인 결과를 1회성으로 가져감
	 */
	@Operation(
		summary = "카카오 로그인 결과 조회 (Bridge Key 소비)",
		description = """
			프론트엔드 브릿지 페이지에서 전달받은 임시 Key를 사용하여 실제 로그인 결과(Access/Refresh Token, 유저 정보)를 조회한다.
			이 Key는 단 한 번만 조회 가능하며, 조회 즉시 저장소에서 삭제(Consume)된다.
			"""
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "로그인 결과 조회 성공"),
		@ApiResponse(responseCode = "400", description = "유효하지 않거나 만료된 Key", content = @Content)
	})
	@GetMapping("/login-result")
	public DataResponse<KakaoLoginResponse> loginResult(
		@Parameter(description = "리다이렉트 URL로 전달받은 1회용 임시 Key", required = true)
		@RequestParam("key") String key
	) {

		KakaoLoginResponse response = loginBridgeStore.consume(key);
		log.info("브릿지 로그인 결과 조회:  response={}", response);
		if (response == null) {
			// TTL 만료/이미 소비/잘못된 key 등
			throw BaseException.from(GlobalErrorCode.INVALID_REQUEST);
		}

		return DataResponse.from(response);
	}

	@Operation(
		summary = "Access 토큰 재발급",
		description = "Refresh 토큰을 검증해 Access(및 필요 시 Refresh) 토큰을 재발급한다."
	)
	@SecurityRequirement(name = "RefreshToken")
	@PostMapping("/refresh")
	public DataResponse<TokenReissueResponse> refresh(
		@Parameter(hidden = true)
		@RequestAttribute(ATTR_REFRESH_CLAIMS) Claims refreshClaims
	) {
		TokenReissueResponse response = tokenReissueService.reissue(refreshClaims);
		return DataResponse.from(response);
	}
}
