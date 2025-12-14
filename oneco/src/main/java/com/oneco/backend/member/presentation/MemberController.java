package com.oneco.backend.member.presentation;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
public class MemberController {

	@GetMapping("/health")
	public String healthCheck() {
		return "멤버 서비스 정상 작동 중";
	}

}
