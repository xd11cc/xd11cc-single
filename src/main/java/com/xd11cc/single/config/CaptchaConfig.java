package com.xd11cc.single.config;

import cloud.tianai.captcha.common.constant.CaptchaTypeConstant;
import cloud.tianai.captcha.resource.CrudResourceStore;
import cloud.tianai.captcha.resource.ResourceStore;
import cloud.tianai.captcha.resource.common.model.dto.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author xd11cc
 * @date 2026-01-19 21:37:33
 * tianai-captcha配置类
 */
@Component
@RequiredArgsConstructor
public class CaptchaConfig {

    private final ResourceStore resourceStore;

    @PostConstruct
    public void init() {

        /*// 滑块验证码 模板 (系统内置) ,这里添加的模板等同于  captcha.init-default-resource=true , 如果配置中设置了加载默认模板，这里模板可不用配置
        ResourceMap template1 = new ResourceMap("default", 4);
        template1.put("active.png", new Resource(ClassPathResourceProvider.NAME, DEFAULT_SLIDER_IMAGE_TEMPLATE_PATH.concat("/1/active.png")));
        template1.put("fixed.png", new Resource(ClassPathResourceProvider.NAME, DEFAULT_SLIDER_IMAGE_TEMPLATE_PATH.concat("/1/fixed.png")));
        ResourceMap template2 = new ResourceMap("default", 4);
        template2.put("active.png", new Resource(ClassPathResourceProvider.NAME, DEFAULT_SLIDER_IMAGE_TEMPLATE_PATH.concat("/2/active.png")));
        template2.put("fixed.png", new Resource(ClassPathResourceProvider.NAME, DEFAULT_SLIDER_IMAGE_TEMPLATE_PATH.concat("/2/fixed.png")));
        // 旋转验证码 模板 (系统内置)
        ResourceMap template3 = new ResourceMap("default", 4);
        template3.put("active.png", new Resource(ClassPathResourceProvider.NAME, StandardSliderImageCaptchaGenerator.DEFAULT_SLIDER_IMAGE_TYPE.concat("/3/active.png")));
        template3.put("fixed.png", new Resource(ClassPathResourceProvider.NAME, StandardSliderImageCaptchaGenerator.DEFAULT_SLIDER_IMAGE_TYPE.concat("/3/fixed.png")));
*/
        CrudResourceStore crudResourceStore = (CrudResourceStore)resourceStore;

        /*// 1. 添加一些模板
        crudResourceStore.addTemplate(CaptchaTypeConstant.SLIDER, template1);
        crudResourceStore.addTemplate(CaptchaTypeConstant.SLIDER, template2);
        crudResourceStore.addTemplate(CaptchaTypeConstant.ROTATE, template3);*/
        // 2. 添加自定义背景图片, resource 的参数1为资源类型(默认支持 classpath/file/url ), resource 的参数2为资源路径, resource 的参数3为标签
        crudResourceStore.addResource(CaptchaTypeConstant.SLIDER, new Resource("classpath", "static/images/tianai-captcha-slider-background.png", "default"));
//        crudResourceStore.addResource(CaptchaTypeConstant.ROTATE, new Resource("classpath", "bgimages/48.jpg", "default"));
//        crudResourceStore.addResource(CaptchaTypeConstant.CONCAT, new Resource("classpath", "bgimages/48.jpg", "default"));
//        crudResourceStore.addResource(CaptchaTypeConstant.WORD_IMAGE_CLICK, new Resource("classpath", "bgimages/c.jpg", "default"));
    }
}
