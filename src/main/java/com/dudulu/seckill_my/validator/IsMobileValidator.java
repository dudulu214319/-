package com.dudulu.seckill_my.validator;

import com.dudulu.seckill_my.utils.ValidatorUtil;
import org.thymeleaf.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @program: SecKill_my
 * @description: 验证手机号
 **/
public class IsMobileValidator implements ConstraintValidator<IsMobile, String> { // 人家ConstraintValidator这个接口给你定义好了一些方法，你实现人家，重写自己的验证机制

    private boolean required = false;

    @Override
    public void initialize(IsMobile constraintAnnotation) {
//        ConstraintValidator.super.initialize(constraintAnnotation);
        required = constraintAnnotation.required();
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if (required) {
            return ValidatorUtil.isMobile(s);
        } else {
            if (StringUtils.isEmpty(s)) {
                return true;
            } else {
                return ValidatorUtil.isMobile(s);
            }
        }
    }
}
