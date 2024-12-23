package PsychologicalCounselingPlatform.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class LogAspect {

    @Around("execution(* PsychologicalCounselingPlatform.controller..*.*(..))")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return point.proceed();
        }
        HttpServletRequest request = attributes.getRequest();
        
        String url = request.getRequestURL().toString();
        String method = request.getMethod();
        String ip = request.getRemoteAddr();
        String className = point.getSignature().getDeclaringTypeName();
        String methodName = point.getSignature().getName();
        Object[] args = point.getArgs();
        
        log.info("请求开始 - URL: {}, Method: {}, IP: {}, Class: {}, Method: {}, Args: {}",
                url, method, ip, className, methodName, Arrays.toString(args));
        
        Object result = point.proceed();
        
        long endTime = System.currentTimeMillis();
        log.info("请求结束 - URL: {}, 耗时: {}ms", url, (endTime - startTime));
        
        return result;
    }
} 