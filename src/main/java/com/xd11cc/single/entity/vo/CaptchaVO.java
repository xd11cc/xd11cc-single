package com.xd11cc.single.entity.vo;

import lombok.Data;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author xd11cc
 * @date 2026-04-15 15:43:01
 * @description
 */
@Data
public class CaptchaVO {

    private String captchaId;

    private String image;
}
