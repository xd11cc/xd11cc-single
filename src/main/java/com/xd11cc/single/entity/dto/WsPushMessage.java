package com.xd11cc.single.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author xd11cc
 * @date 2026-05-29
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WsPushMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private String action;

    private Object data;

    public static WsPushMessage of(String action, Object data) {
        return new WsPushMessage(action, data);
    }
}
