package com.oneco.backend.mission.presentation;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/missions")
public class MissionController {

	@GetMapping("/health")
	public String healthCheck() {
		return "미션 서비스 정상 작동 중";
	}

}
