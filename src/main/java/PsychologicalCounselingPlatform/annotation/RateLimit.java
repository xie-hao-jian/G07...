package PsychologicalCounselingPlatform.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {
    String key() default "";     // 限流key
    int limit() default 10;      // 限流次数
    int period() default 1;      // 限流时间，默认1秒
} 