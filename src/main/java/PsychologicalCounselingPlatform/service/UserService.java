package PsychologicalCounselingPlatform.service;

import PsychologicalCounselingPlatform.dto.LoginRequest;
import PsychologicalCounselingPlatform.dto.LoginResponse;
import PsychologicalCounselingPlatform.entity.User;


public interface UserService {
    LoginResponse login(LoginRequest request);
    User register(LoginRequest request);
    void logout(String token);
    void resetPassword(String username, String newPassword);
    User getUserByUsername(String username);
    User getProfile();
    User updateBasicInfo(User user);
    boolean verifyPassword(String password);
    void updatePassword(String currentPassword, String newPassword);
} 