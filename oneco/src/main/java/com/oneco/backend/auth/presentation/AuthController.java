package com.oneco.backend.auth.presentation;

import static com.oneco.backend.global.security.jwt.filter.RefreshTokenFilter.*;

import java.net.URI;
import java.util.Enumeration;
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
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "인증 관련 API")
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
		@Parameter(hidden = true) HttpServletRequest request
	) {
		// ✅ 쿠키에서 JSESSIONID 확인
		String jsessionidCookie = null;
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie c : cookies) {
				if ("JSESSIONID".equalsIgnoreCase(c.getName())) {
					jsessionidCookie = c.getValue();
					break;
				}
			}
		}
		String jsessionidMasked = (jsessionidCookie != null
			? jsessionidCookie.substring(0, Math.min(12, jsessionidCookie.length())) + "..."
			: "null");

		// ✅ 요청/호스트/프록시/세션 진단
		log.info("[카카오 로그인 시작] 요청 정보: method={} url={}{}",
			request.getMethod(),
			request.getRequestURL(),
			(request.getQueryString() != null ? "?" + request.getQueryString() : "")
		);
		log.info("[카카오 로그인 시작] 접속 정보: scheme={} secure={} serverName={} serverPort={} Host헤더={}",
			request.getScheme(),
			request.isSecure(),
			request.getServerName(),
			request.getServerPort(),
			request.getHeader("Host")
		);
		log.info("[카카오 로그인 시작] 프록시 헤더: X-Forwarded-Proto={} X-Forwarded-Host={} X-Forwarded-For={}",
			request.getHeader("X-Forwarded-Proto"),
			request.getHeader("X-Forwarded-Host"),
			request.getHeader("X-Forwarded-For")
		);
		log.info("[카카오 로그인 시작] 브라우저 정보: Origin={} Referer={} UA={}",
			request.getHeader("Origin"),
			request.getHeader("Referer"),
			request.getHeader("User-Agent")
		);
		log.info("[카카오 로그인 시작] 세션 요청 ID: requestedSessionId={} valid={} fromCookie={} (Cookie JSESSIONID={})",
			request.getRequestedSessionId(),
			request.isRequestedSessionIdValid(),
			request.isRequestedSessionIdFromCookie(),
			jsessionidMasked
		);
		log.info("[카카오 로그인 시작] 서버 세션: sessionId={} isNew={} creationTime={} lastAccessedTime={}",
			session.getId(),
			session.isNew(),
			session.getCreationTime(),
			session.getLastAccessedTime()
		);

		String redirectUri = kakaoOAuthProperties.getRedirectUri();
		log.info("[카카오 로그인 시작] 카카오 redirectUri(설정)={}", redirectUri);
		log.info("[카카오 로그인 시작] 카카오 baseUrl(설정)={}", kakaoOAuthProperties.getBaseUrl());

		// clientId는 민감할 수 있어서 앞부분만
		String clientId = kakaoOAuthProperties.getClientId();
		String clientIdMasked = (clientId != null ? clientId.substring(0, Math.min(6, clientId.length())) + "..." : "null");
		log.info("[카카오 로그인 시작] 카카오 clientId(앞6자리)={}", clientIdMasked);

		// state 생성 + 세션에 저장
		String state = UUID.randomUUID().toString();
		String stateMasked = state.substring(0, Math.min(12, state.length())) + "...";
		log.info("[카카오 로그인 시작] 생성한 state={}", stateMasked);

		session.setAttribute(KAKAO_OAUTH_STATE_SESSION_KEY, state);

		// ✅ 저장 직후 read-back
		String saved = (String) session.getAttribute(KAKAO_OAUTH_STATE_SESSION_KEY);
		String savedMasked = (saved != null ? saved.substring(0, Math.min(12, saved.length())) + "..." : "null");
		log.info("[카카오 로그인 시작] state 세션 저장 여부={} (readBack={})", (saved != null), savedMasked);

		// ✅ 세션 attribute 이름만 덤프(값은 출력 X)
		StringBuilder attrNames = new StringBuilder();
		Enumeration<String> names = session.getAttributeNames();
		while (names.hasMoreElements()) {
			if (attrNames.length() > 0) attrNames.append(", ");
			attrNames.append(names.nextElement());
		}
		log.info("[카카오 로그인 시작] 세션 attribute 목록(이름만)=[{}]", attrNames);

		// scope 구성
		String scope = "openid profile_nickname profile_image";
		log.info("[카카오 로그인 시작] 요청 scope={}", scope);

		URI kakaoAuthorizeUri = UriComponentsBuilder
			.fromUriString(kakaoOAuthProperties.getBaseUrl())
			.path("/oauth/authorize")
			.queryParam("response_type", "code")
			.queryParam("client_id", clientId)
			.queryParam("redirect_uri", redirectUri)
			.queryParam("scope", scope)
			.queryParam("state", state)
			.build()
			.encode()
			.toUri();

		log.info("[카카오 로그인 시작] 카카오 인가 페이지로 리다이렉트 URL={}", kakaoAuthorizeUri);

		HttpHeaders headers = new HttpHeaders();
		headers.setLocation(kakaoAuthorizeUri);

		return ResponseEntity.status(HttpStatus.FOUND)
			.headers(headers)
			.build();
	}

	@Operation(summary = "카카오 로그인 콜백")
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
		@Parameter(hidden = true) HttpSession session,
		@Parameter(hidden = true) HttpServletRequest request
	) {
		// ✅ 쿠키에서 JSESSIONID 확인
		String jsessionidCookie = null;
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie c : cookies) {
				if ("JSESSIONID".equalsIgnoreCase(c.getName())) {
					jsessionidCookie = c.getValue();
					break;
				}
			}
		}
		String jsessionidMasked = (jsessionidCookie != null
			? jsessionidCookie.substring(0, Math.min(12, jsessionidCookie.length())) + "..."
			: "null");

		// ✅ 콜백 요청/호스트/프록시/세션 진단
		log.info("[카카오 콜백] 요청 정보: method={} url={}{}",
			request.getMethod(),
			request.getRequestURL(),
			(request.getQueryString() != null ? "?" + request.getQueryString() : "")
		);
		log.info("[카카오 콜백] 접속 정보: scheme={} secure={} serverName={} serverPort={} Host헤더={}",
			request.getScheme(),
			request.isSecure(),
			request.getServerName(),
			request.getServerPort(),
			request.getHeader("Host")
		);
		log.info("[카카오 콜백] 프록시 헤더: X-Forwarded-Proto={} X-Forwarded-Host={} X-Forwarded-For={}",
			request.getHeader("X-Forwarded-Proto"),
			request.getHeader("X-Forwarded-Host"),
			request.getHeader("X-Forwarded-For")
		);
		log.info("[카카오 콜백] 브라우저 정보: Origin={} Referer={} UA={}",
			request.getHeader("Origin"),
			request.getHeader("Referer"),
			request.getHeader("User-Agent")
		);
		log.info("[카카오 콜백] 세션 요청 ID: requestedSessionId={} valid={} fromCookie={} (Cookie JSESSIONID={})",
			request.getRequestedSessionId(),
			request.isRequestedSessionIdValid(),
			request.isRequestedSessionIdFromCookie(),
			jsessionidMasked
		);
		log.info("[카카오 콜백] 서버 세션: sessionId={} isNew={} creationTime={} lastAccessedTime={}",
			session.getId(),
			session.isNew(),
			session.getCreationTime(),
			session.getLastAccessedTime()
		);

		String stateMasked = (state != null ? state.substring(0, Math.min(12, state.length())) + "..." : "null");
		log.info("[카카오 콜백] 받은 파라미터: code길이={} state={}",
			(code != null ? code.length() : 0),
			stateMasked
		);

		String expectedState = (String) session.getAttribute(KAKAO_OAUTH_STATE_SESSION_KEY);
		String expectedMasked = (expectedState != null ? expectedState.substring(0, Math.min(12, expectedState.length())) + "..." : "null");

		log.info("[카카오 콜백] 세션에서 꺼낸 expectedState={}", expectedMasked);
		log.info("[카카오 콜백] state 일치 여부={}", (expectedState != null && expectedState.equals(state)));

		// ✅ 로그 확인 후 제거(디버깅 시점에서는 이 순서가 안전)
		session.removeAttribute(KAKAO_OAUTH_STATE_SESSION_KEY);
		log.info("[카카오 콜백] expectedState 세션 제거 완료. 현재 존재 여부={}",
			(session.getAttribute(KAKAO_OAUTH_STATE_SESSION_KEY) != null)
		);

		KakaoLoginResponse response = authService.loginWithKakao(code, state, expectedState);

		// ✅ 브릿지 저장소에 저장하고 key 발급
		String key = loginBridgeStore.save(response);
		String keyMasked = (key != null ? key.substring(0, Math.min(10, key.length())) + "..." : "null");
		log.info("[카카오 콜백] 브릿지 저장 완료. key(앞10자리)={}", keyMasked);

		String frontendBase = frontendProperties.getBaseUrl();
		log.info("[카카오 콜백] 프론트 baseUrl(설정)={}", frontendBase);

		URI redirect = UriComponentsBuilder
			.fromUriString(frontendBase)
			.path("/login-bridge")
			.queryParam("key", key)
			.build()
			.toUri();

		log.info("[카카오 콜백] 프론트 브릿지 페이지로 리다이렉트 URL={}", redirect);

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
		@RequestParam("key") String key,
		@Parameter(hidden = true) HttpServletRequest request
	) {
		String keyMasked = (key != null ? key.substring(0, Math.min(10, key.length())) + "..." : "null");

		log.info("[브릿지 결과 조회] 요청: method={} url={}{} Host헤더={} Origin={} Referer={}",
			request.getMethod(),
			request.getRequestURL(),
			(request.getQueryString() != null ? "?" + request.getQueryString() : ""),
			request.getHeader("Host"),
			request.getHeader("Origin"),
			request.getHeader("Referer")
		);
		log.info("[브릿지 결과 조회] consume key(앞10자리)={}", keyMasked);

		KakaoLoginResponse response = loginBridgeStore.consume(key);

		log.info("[브릿지 결과 조회] consume 결과: responseNull={}", (response == null));

		if (response == null) {
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
		@RequestAttribute(ATTR_REFRESH_CLAIMS) Claims refreshClaims,
		@Parameter(hidden = true) HttpServletRequest request
	) {
		log.info("[토큰 재발급] 요청: method={} url={}{} Host헤더={} X-Forwarded-Proto={} X-Forwarded-Host={}",
			request.getMethod(),
			request.getRequestURL(),
			(request.getQueryString() != null ? "?" + request.getQueryString() : ""),
			request.getHeader("Host"),
			request.getHeader("X-Forwarded-Proto"),
			request.getHeader("X-Forwarded-Host")
		);

		// 민감정보 최소화: subject만 출력
		String sub = refreshClaims != null ? refreshClaims.getSubject() : null;
		log.info("[토큰 재발급] refreshClaims subject={}", sub);

		TokenReissueResponse response = tokenReissueService.reissue(refreshClaims);
		log.info("[토큰 재발급] 재발급 성공");

		return DataResponse.from(response);
	}
}
