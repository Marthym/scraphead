package fr.ght1pc9kc.scraphead.netty.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.LastHttpContent;

import java.util.function.BiConsumer;

public class HeadChannelHandler implements BiConsumer<ChannelHandlerContext, Object> {
    @Override
    public void accept(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof ByteBufHolder bbh) {
            if (msg instanceof FullHttpMessage) {
                ctx.fireChannelRead(bbh);
            } else {
                ByteBuf bb = bbh.content();
                ctx.fireChannelRead(bb);
                if (msg instanceof LastHttpContent) {
                    ctx.fireChannelRead(LastHttpContent.EMPTY_LAST_CONTENT);
                }
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }
}
