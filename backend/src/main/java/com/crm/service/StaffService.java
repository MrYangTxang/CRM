package com.crm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.BusinessException;
import com.crm.entity.Staff;
import com.crm.mapper.StaffMapper;
import com.crm.util.ValidationUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class StaffService {

    @Autowired
    private StaffMapper staffMapper;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Transactional
    public boolean add(Staff staff) {
        validateStaff(staff);
        ValidationUtil.requireNotBlank(staff.getPassword(), "密码");
        if (staff.getPassword().length() < 6) {
            throw new BusinessException("密码长度不能少于6位");
        }
        staff.setPassword(encoder.encode(staff.getPassword()));
        return staffMapper.insert(staff) > 0;
    }

    @Transactional
    public boolean update(Staff staff) {
        validateStaff(staff);
        LambdaUpdateWrapper<Staff> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Staff::getId, staff.getId())
                .set(Staff::getName, staff.getName())
                .set(Staff::getUsername, staff.getUsername())
                .set(Staff::getRole, staff.getRole())
                .set(Staff::getPhone, staff.getPhone());
        if (staff.getPassword() != null && !staff.getPassword().trim().isEmpty()) {
            if (staff.getPassword().length() < 6) {
                throw new BusinessException("密码长度不能少于6位");
            }
            wrapper.set(Staff::getPassword, encoder.encode(staff.getPassword()));
        }
        return staffMapper.update(null, wrapper) > 0;
    }

    private void validateStaff(Staff staff) {
        ValidationUtil.requireNotBlank(staff.getName(), "姓名");
        ValidationUtil.requireNotBlank(staff.getUsername(), "用户名");
        if (staff.getPhone() != null && !staff.getPhone().trim().isEmpty()) {
            ValidationUtil.requireValidPhone(staff.getPhone());
        }
        if (staff.getRole() != null && !"admin".equals(staff.getRole()) && !"employee".equals(staff.getRole()) && !"sales_manager".equals(staff.getRole())) {
            throw new BusinessException("角色必须是 admin、employee 或 sales_manager");
        }
    }

    public boolean login(String username, String rawPassword, HttpServletRequest request) {
        Staff staff = staffMapper.findByUsername(username);
        if (staff == null) {
            log.warn("登录失败: 用户名 {} 不存在, IP={}", username, getClientIp(request));
            return false;
        }
        boolean matched = encoder.matches(rawPassword, staff.getPassword());
        if (matched) {
            staff.setLastLoginTime(LocalDateTime.now());
            staff.setLastLoginIp(getClientIp(request));
            staffMapper.updateById(staff);
            log.info("登录成功: username={}, IP={}", username, staff.getLastLoginIp());
        } else {
            log.warn("登录失败: username={} 密码错误, IP={}", username, getClientIp(request));
        }
        return matched;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    @Transactional(readOnly = true)
    public List<Staff> listAll() {
        return staffMapper.selectList(null);
    }

    // ---------------------- 搜索引擎 ----------------------

    @Transactional(readOnly = true)
    public IPage<Staff> search(String keyword, String role, Integer page, Integer size) {
        LambdaQueryWrapper<Staff> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w
                    .like(Staff::getName, keyword)
                    .or().like(Staff::getUsername, keyword)
                    .or().like(Staff::getPhone, keyword)
            );
        }
        if (role != null && !role.trim().isEmpty()) {
            wrapper.eq(Staff::getRole, role);
        }
        wrapper.orderByAsc(Staff::getId);
        return staffMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Transactional(readOnly = true)
    public Staff getById(Integer id) {
        return staffMapper.selectById(id);
    }

    @Transactional
    public boolean delete(Integer id) {
        return staffMapper.deleteById(id) > 0;
    }

    @Transactional(readOnly = true)
    public Staff findByUsername(String username) {
        return staffMapper.findByUsername(username);
    }

    @Transactional
    public boolean updateRole(Integer id, String role, String currentUsername) {
        if (!"admin".equals(role) && !"employee".equals(role) && !"sales_manager".equals(role)) {
            throw new BusinessException("角色必须是 admin、employee 或 sales_manager");
        }
        Staff staff = staffMapper.selectById(id);
        if (staff == null) return false;
        if (currentUsername != null && currentUsername.equals(staff.getUsername())) {
            throw new BusinessException("管理员不能修改自己的角色");
        }
        staff.setRole(role);
        return staffMapper.updateById(staff) > 0;
    }

    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        Staff staff = staffMapper.findByUsername(username);
        if (staff == null) {
            throw new BusinessException("用户不存在");
        }
        if (!encoder.matches(oldPassword, staff.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new BusinessException("新密码长度不能少于6位");
        }
        staff.setPassword(encoder.encode(newPassword));
        staffMapper.updateById(staff);
    }

    @Transactional
    public void resetPassword(String username, String newPassword) {
        Staff staff = staffMapper.findByUsername(username);
        if (staff == null) {
            throw new BusinessException("用户名不存在");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new BusinessException("新密码长度不能少于6位");
        }
        staff.setPassword(encoder.encode(newPassword));
        staffMapper.updateById(staff);
    }
}
