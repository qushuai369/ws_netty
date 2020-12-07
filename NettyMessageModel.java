package com.example.demo.netty;

public class NettyMessageModel {
    private  int roomid;
    private  int uid;
    private String msg;

    public NettyMessageModel(int roomid, int uid, String msg) {
        this.roomid = roomid;
        this.uid = uid;
        this.msg = msg;
    }

    public int getRoomid() {
        return roomid;
    }

    public void setRoomid(int roomid) {
        this.roomid = roomid;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
