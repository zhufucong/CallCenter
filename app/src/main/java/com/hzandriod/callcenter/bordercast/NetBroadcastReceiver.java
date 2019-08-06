package com.hzandriod.callcenter.bordercast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.hzandriod.callcenter.helper.NetEvent;
import com.hzandriod.callcenter.helper.NetUtil;

public class NetBroadcastReceiver extends BroadcastReceiver {

    private NetEvent netEvent;

    @Override
    public void onReceive(Context context, Intent intent) {
        //onReceive这里写上相关的处理代码，一般来说，不要此添加过多的逻辑或者是进行任何的耗时操作
        //因为广播接收器中是不允许开启多线程的，过久的操作就会出现报错
        //广播接收器更多的是扮演一种打开程序其他组件的角色，比如创建一条状态栏通知，或者启动某个服务

        //判断广播的类型为网络action后
        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            //获取当前网络状态，并将结果发送给广播接受者
            int netWrokState = NetUtil.getNetWorkState(context);
            if (netEvent != null)
                netEvent.onNetChange(netWrokState);
        }
    }

    public void setNetEvent(NetEvent netEvent) {
        this.netEvent = netEvent;
    }
}
