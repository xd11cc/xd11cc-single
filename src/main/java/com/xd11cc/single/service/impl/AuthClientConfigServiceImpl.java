package com.xd11cc.single.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xd11cc.single.config.exception.ServiceException;
import com.xd11cc.single.convert.AuthClientConfigConvert;
import com.xd11cc.single.entity.domain.AuthClientConfigDO;
import com.xd11cc.single.entity.vo.AuthClientConfigAddVO;
import com.xd11cc.single.entity.vo.AuthClientConfigQueryVO;
import com.xd11cc.single.entity.vo.AuthClientConfigUpdateVO;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.mapper.AuthClientConfigMapper;
import com.xd11cc.single.service.IAuthClientConfigService;
import com.xd11cc.single.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author xd11cc
 * @date 2026-05-20
 */
@Slf4j
@Service
public class AuthClientConfigServiceImpl extends ServiceImpl<AuthClientConfigMapper, AuthClientConfigDO> implements IAuthClientConfigService {

    @Override
    public AuthClientConfigDO getBySource(String source) {
        LambdaQueryWrapper<AuthClientConfigDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuthClientConfigDO::getSource, source);
        return baseMapper.selectOne(wrapper);
    }

    @Override
    public int add(AuthClientConfigAddVO authClientConfigAddVO) {
        AuthClientConfigDO authClientConfigDO = AuthClientConfigConvert.CONVERT.addVO2DO(authClientConfigAddVO);
        try {
            return baseMapper.insert(authClientConfigDO);
        } catch (DuplicateKeyException e) {
            throw new ServiceException(SystemErrorEnum.AUTH_SOURCE_EXISTS);
        }
    }

    @Override
    public int deleteByIds(List<Long> ids) {
        return baseMapper.deleteBatchIds(ids);
    }

    @Override
    public int modifyById(AuthClientConfigUpdateVO authClientConfigUpdateVO) {
        AuthClientConfigDO authClientConfigDO = AuthClientConfigConvert.CONVERT.updateVO2DO(authClientConfigUpdateVO);
        try {
            return baseMapper.updateById(authClientConfigDO);
        } catch (DuplicateKeyException e) {
            throw new ServiceException(SystemErrorEnum.AUTH_SOURCE_EXISTS);
        }
    }

    @Override
    public List<AuthClientConfigDO> getPageList(AuthClientConfigQueryVO authClientConfigQueryVO) {
        LambdaQueryWrapper<AuthClientConfigDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotEmpty(authClientConfigQueryVO.getSource()),
                AuthClientConfigDO::getSource, authClientConfigQueryVO.getSource());
        wrapper.like(StringUtils.isNotEmpty(authClientConfigQueryVO.getName()),
                AuthClientConfigDO::getName, authClientConfigQueryVO.getName());
        wrapper.eq(StringUtils.isNotEmpty(authClientConfigQueryVO.getStatus()),
                AuthClientConfigDO::getStatus, authClientConfigQueryVO.getStatus());
        wrapper.orderByAsc(AuthClientConfigDO::getSort);
        return baseMapper.selectList(wrapper);
    }
}
