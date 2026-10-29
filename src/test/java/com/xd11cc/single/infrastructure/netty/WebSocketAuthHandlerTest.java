package com.xd11cc.single.infrastructure.netty;

import com.xd11cc.single.entity.domain.SystemUserDO;
import com.xd11cc.single.entity.dto.LoginUserDTO;
import com.xd11cc.single.service.TokenService;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * {@link WebSocketAuthHandler} 的握手鉴权测试。
 * <p>
 * 覆盖四条路径：缺 token、token 无效（返回 null）、token 校验抛异常、
 * 用户信息缺失（systemUserDO 为空）。最后一条是回归用例——早先的判断顺序会在这里 NPE，
 * 被外层 catch 吞成「token验证失败」，排查方向会被带偏。
 * </p>
 *
 * @author xd11cc
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WebSocketAuthHandlerTest {

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private WebSocketAuthHandler handler;

    private EmbeddedChannel channel;

    @BeforeEach
    void setUp() {
        channel = new EmbeddedChannel(handler);
    }

    @AfterEach
    void tearDown() {
        channel.finishAndReleaseAll();
    }

    @Test
    @DisplayName("缺少 token 参数：401 并关闭连接")
    void missingToken_unauthorized() {
        channel.writeInbound(request("/ws"));

        assertUnauthorized("缺少token");
    }

    @Test
    @DisplayName("token 无效（返回 null）：401")
    void invalidToken_unauthorized() {
        given(tokenService.getLoginUser("bad")).willReturn(null);

        channel.writeInbound(request("/ws?token=bad"));

        assertUnauthorized("token无效");
    }

    @Test
    @DisplayName("token 校验抛异常：401 且消息包含验证失败")
    void tokenServiceThrows_unauthorized() {
        given(tokenService.getLoginUser("boom")).willThrow(new IllegalStateException("redis down"));

        channel.writeInbound(request("/ws?token=boom"));

        assertUnauthorized("token验证失败");
    }

    @Test
    @DisplayName("用户信息缺失（systemUserDO 为空）：401，不应抛 NPE")
    void missingSystemUser_unauthorizedWithoutNpe() {
        LoginUserDTO broken = new LoginUserDTO();
        broken.setUserId(1L);   // 有 userId，但 systemUserDO 为空
        given(tokenService.getLoginUser("half")).willReturn(broken);

        channel.writeInbound(request("/ws?token=half"));

        // 关键：走的是「用户信息缺失」，而不是被 catch 成「token验证失败」
        assertUnauthorized("token无效");
    }

    @Test
    @DisplayName("鉴权通过：写入登录用户属性、移除自身、不在此处注册通道")
    void validToken_setsLoginUserAndRemovesSelf() {
        given(tokenService.getLoginUser("ok")).willReturn(loginUser(1L, 10L));

        channel.writeInbound(request("/ws?token=ok"));

        LoginUserDTO attr = channel.attr(ChannelManager.LOGIN_USER).get();
        assertThat(attr).isNotNull();
        assertThat(attr.getUserId()).isEqualTo(1L);
        // 通道注册已延后到握手完成，鉴权阶段不应再插一脚
        assertThat(channel.pipeline().get(WebSocketAuthHandler.class)).isNull();
        assertThat((Object) channel.readOutbound()).isNull();
    }

    // ==================== 辅助 ====================

    private void assertUnauthorized(String expectedFragment) {
        FullHttpResponse response = channel.readOutbound();
        assertThat(response).as("应返回一条 HTTP 响应").isNotNull();
        assertThat(response.status()).isEqualTo(HttpResponseStatus.UNAUTHORIZED);
        assertThat(response.content().toString(java.nio.charset.StandardCharsets.UTF_8))
                .contains(expectedFragment);
    }

    private static DefaultFullHttpRequest request(String uri) {
        return new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, uri);
    }

    private static LoginUserDTO loginUser(Long userId, Long tenantId) {
        LoginUserDTO dto = new LoginUserDTO();
        dto.setUserId(userId);
        SystemUserDO userDO = new SystemUserDO();
        userDO.setId(userId);
        userDO.setUsername("admin");
        userDO.setTenantId(tenantId);
        dto.setSystemUserDO(userDO);
        return dto;
    }
}
