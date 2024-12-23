package PsychologicalCounselingPlatform.aspect;

import PsychologicalCounselingPlatform.annotation.RateLimit;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RedisTemplate<String, Object> redisTemplate;

    @Around("@annotation(PsychologicalCounselingPlatform.annotation.RateLimit)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);
        
        String key = "rate_limit:" + rateLimit.key() + ":" + method.getName();
        Integer count = (Integer) redisTemplate.opsForValue().get(key);
        
        if (count == null) {
            redisTemplate.opsForValue().set(key, 1, rateLimit.period(), TimeUnit.SECONDS);
        } else if (count < rateLimit.limit()) {
            redisTemplate.opsForValue().increment(key);
        } else {
            throw new RuntimeException("请求过于频繁，请稍后再试");
        }
        
        return point.proceed();
    }
} 