package com.ctrip.framework.apollo.metaservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName CusExtService
 * @Description
 * @Author jiangshubian
 * @Date 2018/10/10 14:21
 * @Version 1.0
 */
@Configuration
public class CusExtService {

    @Value("${app.eureka.register.suffix.name}")
    private String appEurekaRegisterSuffixName;

    public String getAppEurekaRegisterSuffixName() {
        return appEurekaRegisterSuffixName;
    }
}
