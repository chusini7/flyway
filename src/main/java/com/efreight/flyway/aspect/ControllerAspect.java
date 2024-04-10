package com.efreight.flyway.aspect;


import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author libiao
 */
@Slf4j
@Aspect
@Component
public class ControllerAspect {

    @Pointcut("execution(public * com.efreight..controller..*Controller.*(..))")
    public void controllerEntries() {
        // do nothing
    }

    @Before("controllerEntries()")
    public void before(JoinPoint jp) {
        print(jp);
    }

    private void print(JoinPoint jp) {
        if (log.isDebugEnabled()) {
            HttpServletRequest request = getHttpServletRequest();
            if (Objects.nonNull(request)) {
                log.debug("URL : " + request.getRequestURL().toString());
                log.debug("HTTP_METHOD : " + request.getMethod());
                log.debug("IP : " + request.getRemoteAddr());
            }
        }
        if (log.isInfoEnabled()) {
            MethodSignature sg = (MethodSignature) jp.getSignature();
            String[] parameterNames = sg.getParameterNames();
            Object[] parameterValus = jp.getArgs();
            Map<String, Object> map = new HashMap<>();
            for (int i = 0; i < parameterValus.length; i++) {
                String name = parameterNames[i];
                Object value = parameterValus[i];
                if (value instanceof ServletRequest || value instanceof ServletResponse) {
                    continue;
                }
                if (value instanceof MultipartFile) {
                    MultipartFile file = (MultipartFile) value;
                    map.put(name, file.getOriginalFilename());
                } else {
                    map.put(name, value);
                }
            }

            log.info("{}.{} param: {}", sg.getDeclaringTypeName(), sg.getName(), JSON.toJSON(map));
        }
    }

    @AfterReturning(pointcut = "controllerEntries()", returning = "result")
    public void afterReturning(JoinPoint jp, Object result) {
        if (log.isInfoEnabled()) {
            Signature sg = jp.getSignature();
            log.info("{}.{} return: {}", sg.getDeclaringTypeName(), sg.getName(), JSON.toJSON(result));
        }
    }

    private static HttpServletRequest getHttpServletRequest() {
        RequestAttributes ra = RequestContextHolder.currentRequestAttributes();
        if (ra instanceof ServletRequestAttributes) {
            ServletRequestAttributes sra = (ServletRequestAttributes) ra;
            return sra.getRequest();
        }
        return null;
    }

}
