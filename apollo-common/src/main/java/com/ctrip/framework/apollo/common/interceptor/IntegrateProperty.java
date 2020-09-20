package com.ctrip.framework.apollo.common.interceptor;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @Author jiangshubian
 */
@ConfigurationProperties(prefix = "integrate.db")
public class IntegrateProperty {
    private String prefixTablename;
    private String interestedTables;
    private boolean tablenameTolowercase;


    public String getPrefixTablename() {
        return prefixTablename;
    }

    public void setPrefixTablename(String prefixTablename) {
        this.prefixTablename = prefixTablename;
    }

    public String getInterestedTables() {
        return interestedTables;
    }

    public void setInterestedTables(String interestedTables) {
        this.interestedTables = interestedTables;
    }

    public boolean isTablenameTolowercase() {
        return tablenameTolowercase;
    }

    public void setTablenameTolowercase(boolean tablenameTolowercase) {
        this.tablenameTolowercase = tablenameTolowercase;
    }

    @Override
    public String toString() {
        return "IntegrateProperty{" +
                "prefixTablename='" + prefixTablename + '\'' +
                ", interestedTables='" + interestedTables + '\'' +
                ", tablenameTolowercase=" + tablenameTolowercase +
                '}';
    }
}
