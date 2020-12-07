package com.example.demo.netty;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.*;
import java.util.HashMap;



public class NettyHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {//TextWebSocketFrame是netty用于处理websocket发来的文本对象
    //所有正在连接的channel都会存在这里面，所以也可以间接代表在线的客户端
    public static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    //在线人数
    public static int online;

    //在线的roomid下uid
    public static HashMap<Integer,Integer > uidUid = new HashMap<>();
    //在线的uid与连接关系
    public static HashMap<Integer, ChannelHandlerContext> uidCtx = new HashMap<>();

    public static HashMap<ChannelHandlerContext,Integer> ctxUid = new HashMap<>();

//    public String URL="jdbc:mysql://123.57.73.120:3306/yinzhang_rygs_ne?characterEncoding=UTF-8&serverTimezone=Asia/Shanghai";
//    public String USER="yinzhang_rygs_ne";
//    public String PW="AmA5kPcRhL5izrRf";

    public Connection conn = (new NettyMySql()).ConMySql();

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        JSONObject jsonObject = JSONObject.parseObject(msg.text());
        NettyMessageModel receiveMsg = JSON.toJavaObject(jsonObject,NettyMessageModel.class);
        int uid = receiveMsg.getUid();
        int roomid = receiveMsg.getRoomid();
        String msgText = receiveMsg.getMsg();
        //添加连接池
        uidCtx.get(uid);
        if(uidCtx.get(uid) == null){
            uidCtx.put(uid,ctx);
            ctxUid.put(ctx,uid);
        }
        System.out.println(uidCtx);
        System.out.println(ctxUid);
        if(msgText.equals("")) return;
        //查询数据库确定room里的uid发送
        try {
            String sql = "SELECT * FROM `think_netty_room`";
            Statement st = conn.createStatement();
            String sqlStr="SELECT * FROM `think_netty_room` WHERE roomid="+ roomid;
            ResultSet rs=st.executeQuery(sqlStr);
            int ci = 0;
            while(rs.next()) {
                ci++;
                //发送
                NettyMessageModel message = new  NettyMessageModel(roomid,uid,msgText);
                dataModel dataMessage = new dataModel(1,"成功",message);
                Integer dataUid = Integer.valueOf(rs.getString("uid")).intValue();
                if(uidCtx.get(dataUid) != null) {
                    SendMessage(uidCtx.get(dataUid), dataMessage);
                }
            }
            System.out.println(ci);
            if(ci==0) {
                NettyMessageModel message = new  NettyMessageModel(0,0,"");
                dataModel dataMessage = new dataModel(-1,"roomid错误",message);
                SendMessage(ctx, dataMessage);
            }
            rs.close();
            st.close();
        }catch  (Exception e ){
            System.out.println("roomid为空");
        }
        System.out.println(uidCtx);
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
        //删除连接池
        int uid = ctxUid.get(ctx);
        uidCtx.remove(uid);
        ctxUid.remove(ctx);


//        Set entries = map.entrySet();
//        for (Iterator iterator = entries.iterator();  iterator.hasNext();) {
//            Map.Entry entry = (Map.Entry) iterator.next();
//            int i = (Integer) entry.getValue();
//            if (i == 2) {
//                //就是调用 iterator.remove()方法可以移除掉map中键值对
//                iterator.remove();
//            }
//        }
    }

    //出现异常
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }

    //给某个人发送消息
    private void SendMessage(ChannelHandlerContext ctx, dataModel msg) {
        ctx.channel().writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(msg)));
    }

    //给每个人发送消息,除发消息人外
    private void SendAllMessages(ChannelHandlerContext ctx,dataModel msg) {
        for(Channel channel:channelGroup){
            if(!channel.id().asLongText().equals(ctx.channel().id().asLongText())){
                channel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(msg)));
            }
        }
    }
}


class dataModel {
    int status;
    String msg;
    NettyMessageModel data;

    public dataModel(int status, String msg, NettyMessageModel data) {
        this.status = status;
        this.msg = msg;
        this.data = data;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public NettyMessageModel getData() {
        return data;
    }

    public void setData(NettyMessageModel data) {
        this.data = data;
    }
}
