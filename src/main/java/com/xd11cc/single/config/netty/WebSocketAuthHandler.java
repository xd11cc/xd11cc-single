package com.xd11cc.single.config.netty;

import com.xd11cc.single.entity.dto.LoginUserDTO;
import com.xd11cc.single.service.TokenService;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author xd11cc
 * @date 2026-04-09 15:32:33
 * @description
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class WebSocketAuthHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final AttributeKey<LoginUserDTO> LOGIN_USER_KEY = AttributeKey.valueOf("LOGIN_USER");

    @Autowired
    private TokenService tokenService;
    @Autowired
    private ChannelManager channelManager;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request)  {
        // 解析 URL 中的 token 参数
        QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
        Map<String, List<String>> params = decoder.parameters();
        List<String> tokenList = params.get("token");

        if (tokenList == null || tokenList.isEmpty()) {
            sendErrorResponse(ctx, HttpResponseStatus.UNAUTHORIZED, "缺少token参数");
            return;
        }

        String token = tokenList.get(0);
        // 验证 token
        LoginUserDTO loginUser;
        try {
            loginUser= tokenService.getLoginUser(token);
            if (loginUser == null || loginUser.getUserId() == null || loginUser.getSystemUserDO().getTenantId() == null) {
                sendErrorResponse(ctx, HttpResponseStatus.UNAUTHORIZED, "token无效，用户信息缺失");
                return;
            }
        } catch (Exception e) {
            sendErrorResponse(ctx, HttpResponseStatus.UNAUTHORIZED, "token验证失败" + e.getMessage());
            return;
        }

        channelManager.addChannel(ctx.channel(), loginUser);
        ctx.channel().attr(LOGIN_USER_KEY).set(loginUser);
        ctx.fireChannelRead(request.retain());
        ctx.pipeline().remove(this);
    }

    /**
     * 发送标准化错误响应
     * @param ctx
     * @param status
     * @param msg
     */
    private void sendErrorResponse(ChannelHandlerContext ctx, HttpResponseStatus status, String msg) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                status,
                ctx.alloc().buffer().writeBytes(msg.getBytes(CharsetUtil.UTF_8))
        );
        response.headers()
                .set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8")
                .set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes())
                .set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);

        ctx.writeAndFlush(response).addListener(future -> {
            if (!future.isSuccess()) {
                log.warn("[netty] 认证错误响应发送失败:{},消息:{}", status, msg, future.cause());
            }
            ctx.close();
        });
    }
}
