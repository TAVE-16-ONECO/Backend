package com.oneco.backend.category.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.oneco.backend.category.application.port.in.GetCategoriesUseCase;
import com.oneco.backend.category.presentation.response.CategoriesResponse;
import com.oneco.backend.global.response.DataResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
@Tag(name = "Category", description = "카테고리 조회")
public class CategoryController {

	private final GetCategoriesUseCase getCategoriesUseCase;

	@GetMapping
	@Operation(
		summary = "카테고리 목록 조회",
		description = "모든 카테고리 정보를 조회한다."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "카테고리 목록 조회 성공")
	})
	public ResponseEntity<DataResponse<CategoriesResponse>> getCategories(
	) {
		CategoriesResponse response = CategoriesResponse.from(
			getCategoriesUseCase.getCategories()
		);
		return ResponseEntity.ok(DataResponse.from(response));
	}
}
