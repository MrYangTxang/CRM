package com.crm.aspect;

import com.crm.entity.OperationLog;
import com.crm.mapper.OperationLogMapper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 操作日志切面：拦截所有 Controller 的 @PostMapping、@PutMapping、@DeleteMapping
 */
@Aspect
@Component
public class OperationLogAspect {

    @Autowired
    private OperationLogMapper operationLogMapper;

    @AfterReturning("@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
                    "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
                    "@annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    public void logOperation(JoinPoint joinPoint) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) return;

            HttpServletRequest request = attributes.getRequest();
            HttpSession session = request.getSession(false);

            String username = "anonymous";
            if (session != null) {
                Object loginUser = session.getAttribute("loginUser");
                if (loginUser != null) username = loginUser.toString();
            }

            String method = request.getMethod();
            String url = request.getRequestURI();
            String ip = getClientIp(request);

            // 截取请求参数（最多2000字符）
            String params = "";
            if (joinPoint.getArgs() != null && joinPoint.getArgs().length > 0) {
                // 排除 HttpServletRequest/HttpServletResponse 类型的参数
                params = Arrays.stream(joinPoint.getArgs())
                        .filter(arg -> !(arg instanceof HttpServletRequest)
                                && !(arg instanceof javax.servlet.http.HttpServletResponse))
                        .map(Object::toString)
                        .collect(Collectors.joining(", "));
                if (params.length() > 2000) {
                    params = params.substring(0, 2000);
                }
            }

            OperationLog log = new OperationLog();
            log.setUsername(username);
            log.setMethod(method);
            log.setUrl(url);
            log.setParams(params);
            log.setIp(ip);

            operationLogMapper.insert(log);
        } catch (Exception e) {
            // 日志记录失败不影响业务流程
            System.err.println("[OperationLogAspect] 记录操作日志失败: " + e.getMessage());
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
