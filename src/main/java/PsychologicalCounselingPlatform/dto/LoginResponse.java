package PsychologicalCounselingPlatform.dto;

import PsychologicalCounselingPlatform.entity.User;
import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private User user;
} 