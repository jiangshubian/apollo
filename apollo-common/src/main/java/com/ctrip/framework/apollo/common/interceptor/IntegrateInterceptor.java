package com.ctrip.framework.apollo.common.interceptor;

import com.ctrip.framework.apollo.core.utils.StringUtils;
import org.hibernate.EmptyInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @Author jiangshubian
 */
@Component
@EnableConfigurationProperties(IntegrateProperty.class)
public class IntegrateInterceptor extends EmptyInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(IntegrateInterceptor.class);
    private static final String tableSeparator = "`";

    @Autowired
    private IntegrateProperty dbProperty;

    @Override
    public String onPrepareStatement(String sql) {
        if (StringUtils.isEmpty(sql) || "*".equals(dbProperty.getInterestedTables())){

            return super.onPrepareStatement(sql);
        }

        for (String tbs : dbProperty.getInterestedTables().split(",")) {
            //表名前面必然有空格
            sql = sql.replaceAll(" " + tableSeparator + tbs + tableSeparator, " " +
                    tableSeparator + dbProperty.getPrefixTablename() +
                    (dbProperty.isTablenameTolowercase() ? tbs.toLowerCase() : tbs) +
                    tableSeparator).replaceFirst("Update "+tbs,"Update " +
                    tableSeparator + dbProperty.getPrefixTablename() +
                    (dbProperty.isTablenameTolowercase() ? tbs.toLowerCase() : tbs) +
                    tableSeparator);
        }
        LOG.debug("After prepare sql>>>{}", sql);
        return super.onPrepareStatement(sql);
    }


    @PostConstruct
    public void print() {
        LOG.info("Print IntegrateProperty....{}", dbProperty);
    }
}
