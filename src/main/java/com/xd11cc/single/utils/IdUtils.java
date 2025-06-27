package com.xd11cc.single.utils;

import cn.hutool.core.lang.UUID;

/**
  *@Author: xd11cc
  *@Date: 2025/6/27 22:55
 **/
public class IdUtils {

    public static String fastUUID() {
        return UUID.fastUUID().toString();
    }
}
