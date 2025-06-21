package com.xd11cc.single.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * @Author: xd11cc
 * @Date: 2025/6/21 21:39
 **/
@Data
@NoArgsConstructor
@Component
@ConfigurationProperties("security")
public class SecurityProperties {

    private List<String> permitAllUrls = Collections.emptyList();
}
