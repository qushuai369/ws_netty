package com.example.demo.Controller;

import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;

public class NettyHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {//TextWebSocketFrame是netty用于处理websocket发来的文本对象
    //所有正在连接的channel都会存在这里面，所以也可以间接代表在线的客户端
    public static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    //在线人数
    public static int online;
    //接收到客户都发送的消息
    @Override
    public void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        System.out.println(msg.text());
        Object data = new user("曲帅",18);
        Send_Message message = new  Send_Message(1,"成功",data);
        SendAllMessages(ctx,message);//Send_Message是我的自定义类型，前后端分离往往需要统一数据格式，可以先把对象转成json字符串再发送给客户端
    }
    //客户端建立连接
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        channelGroup.add(ctx.channel());
        online=channelGroup.size();
        System.out.println(ctx.channel().remoteAddress()+"上线了!");
    }
    //关闭连接
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        channelGroup.remove(ctx.channel());
        online=channelGroup.size();
        System.out.println(ctx.channel().remoteAddress()+"断开连接");
    }

    //出现异常
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }

    //给某个人发送消息
    private void SendMessage(ChannelHandlerContext ctx, Send_Message msg) {
        ctx.channel().writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(msg)));
    }

    //给每个人发送消息,除发消息人外
    private void SendAllMessages(ChannelHandlerContext ctx,Send_Message msg) {
        for(Channel channel:channelGroup){
            if(!channel.id().asLongText().equals(ctx.channel().id().asLongText())){
                channel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(msg)));
            }
        }
    }
}

class Send_Message {

    private  int status;
    private String msg;
    private Object data;

    public Send_Message(int status,String msg, Object data) {
        this.status = status;
        this.msg = msg;
        this.data = data;
    }

    public int getStatus() {
        return this.status;
    }
    public String getMsg() {
        return this.msg;
    }
    public Object getData() {
        return this.data;
    }

}

class user {
    private  int age;
    private String name;

    public user(String name, int age) {
        this.age = age;
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public String getName() {
        return name;
    }
}