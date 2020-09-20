package com.ctrip.framework.apollo.common.interceptor;

import org.hibernate.Interceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @Author jiangshubian
 */
@Component
public class HibernateConfiguration implements HibernatePropertiesCustomizer {

    @Autowired
    Interceptor myInterceptor;

    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put("hibernate.session_factory.interceptor", myInterceptor);
    }
}
