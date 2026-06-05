package com.crm.util;

import com.crm.common.BusinessException;

public class ValidationUtil {

    public static final String PHONE_REGEX = "^1[3-9]\\d{9}$";
    public static final String EMAIL_REGEX = "^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$";

    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && phone.matches(PHONE_REGEX);
    }

    public static boolean isValidEmail(String email) {
        return email != null && email.matches(EMAIL_REGEX);
    }

    /** 校验非空，不通过抛 BusinessException */
    public static void requireNotBlank(String value, String fieldName) {
        if (isBlank(value)) {
            throw new BusinessException(fieldName + "不能为空");
        }
    }

    /** 校验手机号格式，不通过抛 BusinessException */
    public static void requireValidPhone(String phone) {
        if (!isValidPhone(phone)) {
            throw new BusinessException("手机号必须为11位手机号码");
        }
    }

    /** 校验邮箱格式，不通过抛 BusinessException */
    public static void requireValidEmail(String email) {
        if (!isValidEmail(email)) {
            throw new BusinessException("邮箱格式不正确");
        }
    }
}
