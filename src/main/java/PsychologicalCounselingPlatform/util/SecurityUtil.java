package PsychologicalCounselingPlatform.util;

import PsychologicalCounselingPlatform.entity.User;
import PsychologicalCounselingPlatform.security.UserDetailsImpl;
import PsychologicalCounselingPlatform.exception.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    public static UserDetailsImpl getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            return (UserDetailsImpl) authentication.getPrincipal();
        }
        return null;
    }

    public static String getCurrentUserId() {
        UserDetailsImpl userDetails = getCurrentUser();
        return userDetails != null ? userDetails.getUser().getId() : null;
    }

    public static User getCurrentUserInfo() {
        UserDetailsImpl userDetails = getCurrentUser();
        return userDetails != null ? userDetails.getUser() : null;
    }

    public static void validateLogin() {
        if (getCurrentUser() == null) {
            throw new BusinessException(401, "请先登录");
        }
    }

    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new RuntimeException("用户未登录");
        }
        return authentication.getName();
    }
} 