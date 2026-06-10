package com.xd11cc.single.entity.base;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author xd11cc
 * @date 2026-06-05 09:57:47
 * @description
 */
@Data
public class PayBaseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 支付场景
     */
    @NotBlank
    private String sceneCode;

    /**
     * 业务ID
     */
    @NotNull
    private Long businessId;

    /**
     * 商品标题
     */
    @NotBlank
    private String subject;

    /**
     * 商品描述
     */
    @NotBlank
    private String content;

    /**
     * 支付金额，单位分
     */
    @NotNull
    private Integer amount;
}
