package com.xd11cc.single.config.properties;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xd11cc
 * @date 2026-01-23 17:02:13
 * @description
 */
@Data
@NoArgsConstructor
@Component
@ConfigurationProperties("tenant")
public class TenantIgnoreProperties {

    private List<String> ignoreTables = new ArrayList<>();

    private List<String> ignoreUrls = new ArrayList<>();
}
