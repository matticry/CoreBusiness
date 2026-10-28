package com.matticry.corebusiness.presentation;

import com.matticry.corebusiness.presentation.dto.ApiResponse;
import com.matticry.corebusiness.presentation.dto.ApiResponseFactory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

    private final ApiResponseFactory apiResponseFactory;

    @GetMapping("/ok")
    ApiResponse<String> ok() {
        return apiResponseFactory.ok("hola");
    }

    @PostMapping("/validation")
    ApiResponse<String> validation(@Valid @RequestBody TestRequest request) {
        return apiResponseFactory.ok(request.name());
    }

    public record TestRequest(@NotBlank String name) {
    }
}
