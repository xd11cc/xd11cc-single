package com.xd11cc.single.entity.dto;

import lombok.Getter;

/**
 * @author xd11cc
 * @date 2026-04-23 15:15:15
 * @description
 */
@Getter
public class GenerateConfigDTO {

    private String entityPackage;

    private String controllerPackage;

    private String servicePackage;

    private String mapperPackage;

    private boolean hasTenant;

    private String author;

    private String tableName;

    public void setEntityPackage(String basePackage) {
        this.entityPackage = basePackage + ".entity.domain";
    }

    public void setControllerPackage(String basePackage) {
        this.controllerPackage = basePackage + ".controller";
    }

    public void setServicePackage(String basePackage) {
        this.servicePackage = basePackage + ".service";
    }

    public void setMapperPackage(String basePackage) {
        this.mapperPackage = basePackage + ".mapper";
    }

    public void setHasTenant(Boolean hasTenant) {
        this.hasTenant = hasTenant;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
}
