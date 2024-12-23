package PsychologicalCounselingPlatform.aspect;

import PsychologicalCounselingPlatform.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

@Aspect
@Component
@RequiredArgsConstructor
public class ValidatorAspect {

    @Before("execution(* PsychologicalCounselingPlatform.controller..*.*(.., org.springframework.validation.BindingResult, ..))")
    public void validateParameters(JoinPoint point) {
        Object[] args = point.getArgs();
        for (Object arg : args) {
            if (arg instanceof BindingResult result) {
                if (result.hasErrors()) {
                    throw new BusinessException(
                        result.getAllErrors().stream()
                            .findFirst()
                            .map(DefaultMessageSourceResolvable::getDefaultMessage)
                            .orElse("参数验证失败")
                    );
                }
            }
        }
    }
} 