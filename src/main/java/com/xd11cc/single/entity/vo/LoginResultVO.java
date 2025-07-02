package com.xd11cc.single.entity.vo;

import lombok.Builder;
import lombok.Data;

/**
 * @Author: xd11cc
 * @Date: 2025/6/14 00:37
 **/
@Data
@Builder
public class LoginResultVO {

    private Long userId;

    private String accessToken;
}
