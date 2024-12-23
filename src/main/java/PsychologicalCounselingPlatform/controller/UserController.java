package PsychologicalCounselingPlatform.controller;

import PsychologicalCounselingPlatform.exception.ApiResponse;
import PsychologicalCounselingPlatform.dto.PasswordUpdateRequest;
import PsychologicalCounselingPlatform.entity.User;
import PsychologicalCounselingPlatform.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ApiResponse<User> getProfile() {
        return ApiResponse.success(userService.getProfile());
    }

    @PutMapping("/profile")
    public ApiResponse<User> updateProfile(@RequestBody User user) {
        try {
            User updatedUser = userService.updateBasicInfo(user);
            return ApiResponse.success(updatedUser);
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }

    @PostMapping("/verify-password")
    public ApiResponse<Boolean> verifyPassword(@RequestBody Map<String, String> params) {
        try {
            boolean isValid = userService.verifyPassword(params.get("password"));
            return ApiResponse.success(isValid);
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }

    @PutMapping("/password")
    public ApiResponse<Void> updatePassword(@RequestBody PasswordUpdateRequest request) {
        try {
            userService.updatePassword(request.getCurrentPassword(), request.getNewPassword());
            return ApiResponse.success(null);
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }
}           