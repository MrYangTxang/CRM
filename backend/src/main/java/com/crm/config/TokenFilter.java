package com.crm.config;

import com.crm.entity.Staff;
import com.crm.mapper.StaffMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Base64;

/**
 * Token 认证过滤器：验证 Bearer Token
 * 对 /api/** 请求进行 Token 校验（登录/注册除外）
 */
@Slf4j
@Component
@Order(1)
public class TokenFilter implements Filter {

    @Autowired
    private StaffMapper staffMapper;

    private static final String[] SKIP_PATHS = {
        "/api/staff/login", "/api/staff/register", "/api/staff/resetPassword"
    };

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String path = request.getRequestURI();

        // 跳过不需要认证的路径
        for (String skip : SKIP_PATHS) {
            if (path.equals(skip)) {
                chain.doFilter(req, res);
                return;
            }
        }

        // 1. 先检查 Session 是否已有 loginUser（已登录状态）
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("loginUser") != null) {
            chain.doFilter(req, res);
            return;
        }

        // 2. 检查 Authorization Header 中的 Token
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                String decoded = new String(Base64.getDecoder().decode(token));
                String[] parts = decoded.split(":");
                if (parts.length == 2) {
                    String username = parts[0];
                    Staff staff = staffMapper.findByUsername(username);
                    if (staff != null) {
                        // Token 有效，恢复 Session
                        session = request.getSession(true);
                        session.setAttribute("loginUser", username);
                        chain.doFilter(req, res);
                        return;
                    }
                }
            } catch (Exception e) {
                log.debug("Token 解析失败: {}", e.getMessage());
            }
        }

        // 3. Token 无效，返回 401
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(401);
        response.getWriter().write("{\"code\":401,\"message\":\"未登录或Token已过期，请重新登录\"}");
    }
}
