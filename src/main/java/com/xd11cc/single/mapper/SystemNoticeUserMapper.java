package com.xd11cc.single.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xd11cc.single.entity.domain.SystemNoticeUserDO;
import com.xd11cc.single.entity.dto.SystemNoticeUserDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author xd11cc
 * @date 2026-05-29
 */
public interface SystemNoticeUserMapper extends BaseMapper<SystemNoticeUserDO> {

    List<SystemNoticeUserDTO> selectMyNoticeList(@Param("userId") Long userId,
                                                 @Param("type") Integer type,
                                                 @Param("readStatus") Integer readStatus,
                                                 @Param("title") String title);

    List<Map<String, Object>> selectUnreadCount(@Param("userId") Long userId);
}
