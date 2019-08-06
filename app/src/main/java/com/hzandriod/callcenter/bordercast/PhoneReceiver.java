package com.hzandriod.callcenter.bordercast;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.hzandriod.callcenter.model.PostData;
import com.hzandriod.callcenter.R;
import com.hzandriod.callcenter.activity.MainActivity;
import com.uzmap.pkg.uzsocket.api.Receiver;

public class PhoneReceiver extends BroadcastReceiver {
    private Context mcontext;
    private WindowManager wm;
    Receiver myReceiver;
    @Override
    public void onReceive(Context context, Intent intent){
        mcontext=context;
        System.out.println("action"+intent.getAction());
        if(intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)){
            //如果是去电（拨出）
            Log.e("TAG","拨出");
        }else{
            //查了下android文档，貌似没有专门用于接收来电的action,所以，非去电即来电
            Log.e("TAG","来电");
            TelephonyManager tm = (TelephonyManager)context.getSystemService(Service.TELEPHONY_SERVICE);
            tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
            //设置一个监听器
        }
    }
    private TextView tv;
    private LayoutInflater inflate;
    private View phoneView;
    private PhoneStateListener listener=new PhoneStateListener(){

        @Override
        public void onCallStateChanged(int state, final String incomingNumber) {
            // TODO Auto-generated method stub
            //state 当前状态 incomingNumber,貌似没有去电的API
            int ism=0;
            PostData data = new PostData();
            data.setPhoneNumber(incomingNumber);
            data.setPlace("山东临沂");
            data.setCustomer("未知客户");
            data.setCalltype1("呼叫");
            data.setCalltime1("2019-10-15 10:12");
            data.setCallcontent1("这是阙勒霍多，长安十二时辰,午时三刻，巳正为9点钟");
            data.setCalltype2("来电");
            data.setCalltime2("2019-10-18 18:22");
            data.setCallcontent2("世事万万不能两全23asdfadfad阿打发斯蒂芬阿斯蒂芬");
            super.onCallStateChanged(state, incomingNumber);
            switch(state){
                case TelephonyManager.CALL_STATE_IDLE:
                    Log.e("TAG","挂断");
//                    wm.removeView(tv);
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    Log.e("TAG","接听或者拨号去电");
//                    if(ism!=1){
//                        openshell(data);
//                    }
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    ism=1;
                    openshell(data);
                    Log.e("TAG","响铃:来电号码"+incomingNumber);
                    Log.e("TAG","响铃:======"+Thread.currentThread().getName());
                    //输出来电号码
                    break;
            }
        }

    };
    public void openshell(PostData data){
        inflate= LayoutInflater.from(mcontext);
        wm = (WindowManager)mcontext.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.type = WindowManager.LayoutParams.TYPE_PHONE;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.gravity= Gravity.CENTER;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = 500;
        params.width = 500;
        params.format = PixelFormat.RGBA_8888;
        phoneView=inflate.inflate(R.layout.shells_activity,null);
        wm.addView(phoneView, params);
        //通话号码
        TextView topnumber=phoneView.findViewById(R.id.TopNumber);
        topnumber.setText(data.PhoneNumber);
        //通话号码
        TextView Place=phoneView.findViewById(R.id.Place);
        Place.setText(data.Place);
        //客户名称
        TextView customer=phoneView.findViewById(R.id.Customer);
        customer.setText(data.Customer);
        //添加客户
        TextView addbtn=phoneView.findViewById(R.id.Addbtn);
        //未知客户才显示新增
        if(!data.Customer.equals("未知客户")){
            addbtn.setVisibility(View.INVISIBLE);
        }else{
            addbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //打开自定义的Activity
                    Intent intentNotifi = new Intent(mcontext, MainActivity.class);
//                        intentNotifi.putExtra("title", title);
//                        intentNotifi.putExtra("body", body);
                    intentNotifi.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP );
                    mcontext.startActivity(intentNotifi);
                }
            });
        }
        //Calltype1
        TextView calltype1=phoneView.findViewById(R.id.Calltype1);
        calltype1.setText(data.Calltype1);
        //Calltime1
        TextView Calltime1=phoneView.findViewById(R.id.Calltime1);
        Calltime1.setText(data.Calltime1);
        //Callcontent1
        TextView Callontent1=phoneView.findViewById(R.id.Callcontent1);
        Callontent1.setText(data.Callcontent1);
        //Calltype1
        TextView calltype2=phoneView.findViewById(R.id.Calltype2);
        calltype2.setText(data.Calltype2);
        //Calltime1
        TextView Calltime2=phoneView.findViewById(R.id.Calltime2);
        Calltime2.setText(data.Calltime2);
        //Callcontent1
        TextView Callontent2=phoneView.findViewById(R.id.Callcontent2);
        Callontent2.setText(data.Callcontent2);
        //关闭按钮
        TextView closebtn=phoneView.findViewById(R.id.Closebtn);
        closebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wm.removeView(tv);
            }
        });
    }
}
