package PsychologicalCounselingPlatform.controller;

import PsychologicalCounselingPlatform.exception.ApiResponse;
import PsychologicalCounselingPlatform.dto.LoginRequest;
import PsychologicalCounselingPlatform.dto.LoginResponse;
import PsychologicalCounselingPlatform.dto.ResetPasswordRequest;
import PsychologicalCounselingPlatform.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(userService.login(request));
    }
    
    @PostMapping("/register")
    public ApiResponse<LoginResponse> register(@Valid @RequestBody LoginRequest request) {
        userService.register(request);
        return ApiResponse.success(userService.login(request));
    }
    
    @PostMapping("/logout")
    public ApiResponse<?> logout(@RequestHeader("Authorization") String token) {
        userService.logout(token.replace("Bearer ", ""));
        return ApiResponse.success(null);
    }
    
    @PostMapping("/reset-password")
    public ApiResponse<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request.getUsername(), request.getNewPassword());
        return ApiResponse.success(null);
    }
} 