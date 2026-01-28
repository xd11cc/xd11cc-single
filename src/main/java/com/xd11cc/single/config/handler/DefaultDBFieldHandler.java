package com.xd11cc.single.config.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.xd11cc.single.entity.base.BaseDO;
import com.xd11cc.single.utils.SecurityUtils;
import org.apache.ibatis.reflection.MetaObject;

import java.util.Date;
import java.util.Objects;

/**
 * @author xd11cc
 * @date 2026-01-23 16:07:41
 * @description 通用参数填充实现类
 */
public class DefaultDBFieldHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        if (Objects.nonNull(metaObject) && metaObject.getOriginalObject() instanceof BaseDO){
            BaseDO baseDO = (BaseDO) metaObject.getOriginalObject();
            Date now = new Date();
            // 创建时间为空，则以当前时间为插入时间
            if (Objects.isNull(baseDO.getCreateTime())){
                baseDO.setCreateTime(now);
            }
            // 更新时间为空，则以当前时间为更新时间
            if (Objects.isNull(baseDO.getUpdateTime())){
                baseDO.setUpdateTime(now);
            }

            Long userId = SecurityUtils.getUserId();
            // 当前登录用户不为空
            if (Objects.nonNull(userId)){
                // 创建人为空，则当前登录用户为创建人
                if (Objects.isNull(baseDO.getCreateUserId())){
                    baseDO.setCreateUserId(userId);
                }
                // 更新人为空，则当前登录用户为更新人
                if (Objects.isNull(baseDO.getUpdateUserId())){
                    baseDO.setUpdateUserId(userId);
                }
            }
            baseDO.setDelFlag(false);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 不管更新时间是否为空，都更新为当前时间
        setFieldValByName("updateTime", new Date(), metaObject);

        Object modifier = getFieldValByName("updateUserId", metaObject);
        Long userId = SecurityUtils.getUserId();
        if (Objects.nonNull(userId) && Objects.isNull(modifier)){
            setFieldValByName("updateUserId", userId, metaObject);
        }
    }
}
