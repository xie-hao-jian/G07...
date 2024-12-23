package PsychologicalCounselingPlatform.util;

import PsychologicalCounselingPlatform.exception.BusinessException;
import org.springframework.util.StringUtils;

public class ValidationUtil {

    public static void validateNotEmpty(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(fieldName + "不能为空");
        }
    }

    public static void validateNotNull(Object value, String fieldName) {
        if (value == null) {
            throw new BusinessException(fieldName + "不能为空");
        }
    }

    public static void validateLength(String value, int min, int max, String fieldName) {
        if (value != null && (value.length() < min || value.length() > max)) {
            throw new BusinessException(fieldName + "长度必须在" + min + "到" + max + "之间");
        }
    }

    public static void validateRange(Integer value, int min, int max, String fieldName) {
        if (value != null && (value < min || value > max)) {
            throw new BusinessException(fieldName + "必须在" + min + "到" + max + "之间");
        }
    }
} 