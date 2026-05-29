package com.xd11cc.single.convert;

import com.xd11cc.single.entity.domain.SystemNoticeDO;
import com.xd11cc.single.entity.vo.SystemNoticeAddVO;
import com.xd11cc.single.entity.vo.SystemNoticeUpdateVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * @author xd11cc
 * @date 2026-05-29
 */
@Mapper
public interface SystemNoticeConvert {

    SystemNoticeConvert INSTANCE = Mappers.getMapper(SystemNoticeConvert.class);

    @Mapping(target = "scopeDeptIds", expression = "java(vo.getScopeDeptIds() != null ? vo.getScopeDeptIds().stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(\",\")) : null)")
    @Mapping(target = "scopeUserIds", expression = "java(vo.getScopeUserIds() != null ? vo.getScopeUserIds().stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(\",\")) : null)")
    SystemNoticeDO addVO2DO(SystemNoticeAddVO vo);

    @Mapping(target = "scopeDeptIds", expression = "java(vo.getScopeDeptIds() != null ? vo.getScopeDeptIds().stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(\",\")) : null)")
    @Mapping(target = "scopeUserIds", expression = "java(vo.getScopeUserIds() != null ? vo.getScopeUserIds().stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(\",\")) : null)")
    SystemNoticeDO updateVO2DO(SystemNoticeUpdateVO vo);
}
