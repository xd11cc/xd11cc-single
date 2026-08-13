package com.xd11cc.single.service.impl;

import com.xd11cc.single.entity.domain.AuthSocialUserDO;
import com.xd11cc.single.mapper.AuthSocialUserMapper;
import com.xd11cc.single.service.IAuthSocialUserService;
import com.xd11cc.single.util.BaseUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class AuthSocialUserServiceImplTest extends BaseUnitTest {

    @Mock
    private AuthSocialUserMapper baseMapper;

    @InjectMocks
    private AuthSocialUserServiceImpl socialUserService;

    // ==================== getBySourceAndUuid - 正向路径 ====================

    @Test
    void getBySourceAndUuid_存在_返回社交用户() {
        AuthSocialUserDO expected = buildSocialUser(1L, "github", "uuid_123", "open_123", "token_abc");
        given(baseMapper.selectOne(any())).willReturn(expected);

        AuthSocialUserDO result = socialUserService.getBySourceAndUuid("github", "uuid_123");

        assertThat(result).isSameAs(expected);
        assertThat(result.getSource()).isEqualTo("github");
        assertThat(result.getUuid()).isEqualTo("uuid_123");
    }

    @Test
    void getBySourceAndUuid_不存在_返回null() {
        given(baseMapper.selectOne(any())).willReturn(null);

        AuthSocialUserDO result = socialUserService.getBySourceAndUuid("unknown", "uuid_999");

        assertThat(result).isNull();
    }

    @Test
    void getBySourceAndUuid_不同source_独立查询() {
        AuthSocialUserDO github = buildSocialUser(1L, "github", "uuid_123", "open_github", "token_gh");
        AuthSocialUserDO wechat = buildSocialUser(2L, "wechat", "uuid_123", "open_wx", "token_wx");
        given(baseMapper.selectOne(any()))
                .willReturn(github)
                .willReturn(wechat);

        AuthSocialUserDO ghResult = socialUserService.getBySourceAndUuid("github", "uuid_123");
        AuthSocialUserDO wxResult = socialUserService.getBySourceAndUuid("wechat", "uuid_123");

        assertThat(ghResult.getSource()).isEqualTo("github");
        assertThat(ghResult.getOpenId()).isEqualTo("open_github");
        assertThat(wxResult.getSource()).isEqualTo("wechat");
        assertThat(wxResult.getOpenId()).isEqualTo("open_wx");
    }

    // ==================== getBySourceAndUuid - 参数透传验证 ====================

    @Test
    void getBySourceAndUuid_调用selectOne一次_参数非null() {
        AuthSocialUserDO expected = buildSocialUser(1L, "github", "uuid_123", "open_123", "token_abc");
        given(baseMapper.selectOne(any())).willReturn(expected);

        socialUserService.getBySourceAndUuid("github", "uuid_123");

        // 验证 baseMapper.selectOne 被恰好调用 1 次，且参数非 null
        then(baseMapper).should(times(1)).selectOne(any());
        then(baseMapper).should(never()).selectOne(null);
    }

    // ==================== getBySourceAndUuid - 边界参数 ====================

    @Test
    void getBySourceAndUuid_nullSourceType_向下透传() {
        given(baseMapper.selectOne(any())).willReturn(null);

        AuthSocialUserDO result = socialUserService.getBySourceAndUuid(null, "uuid_123");

        assertThat(result).isNull();
        then(baseMapper).should(times(1)).selectOne(any());
    }

    @Test
    void getBySourceAndUuid_nullUuid_向下透传() {
        given(baseMapper.selectOne(any())).willReturn(null);

        AuthSocialUserDO result = socialUserService.getBySourceAndUuid("github", null);

        assertThat(result).isNull();
        then(baseMapper).should(times(1)).selectOne(any());
    }

    @Test
    void getBySourceAndUuid_空字符串参数_向下透传() {
        given(baseMapper.selectOne(any())).willReturn(null);

        AuthSocialUserDO result = socialUserService.getBySourceAndUuid("", "");

        assertThat(result).isNull();
        then(baseMapper).should(times(1)).selectOne(any());
    }

    // ==================== getBySourceAndUuid - 异常传播 ====================

    @Test
    void getBySourceAndUuid_mapper抛异常_向上抛出() {
        given(baseMapper.selectOne(any())).willThrow(new RuntimeException("db error"));

        assertThatThrownBy(() -> socialUserService.getBySourceAndUuid("github", "uuid_123"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("db error");
    }

    // ==================== 辅助方法 ====================

    private static AuthSocialUserDO buildSocialUser(Long id, String source, String uuid,
                                                     String openId, String token) {
        AuthSocialUserDO user = new AuthSocialUserDO();
        user.setId(id);
        user.setSource(source);
        user.setUuid(uuid);
        user.setUserId(id);
        user.setOpenId(openId);
        user.setToken(token);
        user.setRowTokenInfo("raw_token_info");
        user.setNickname("nick_" + source);
        user.setAvatar("avatar_" + source);
        user.setRowUserInfo("{}");
        user.setCode("code");
        user.setState("state");
        user.setRemark("备注");
        user.setTenantId(1L);
        user.setBindTime(new java.util.Date());
        return user;
    }
}
