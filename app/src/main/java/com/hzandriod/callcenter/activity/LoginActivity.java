package com.hzandriod.callcenter.activity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.net.ConnectivityManager;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.security.*;
import java.io.UnsupportedEncodingException;

import com.hzandriod.callcenter.R;
import com.hzandriod.callcenter.constant.UrlConstant;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

import com.hzandriod.callcenter.bordercast.NetBroadcastReceiver;
import com.hzandriod.callcenter.helper.NetEvent;
import com.hzandriod.callcenter.model.ReturnData;

public class LoginActivity extends AppCompatActivity implements NetEvent, View.OnClickListener {

    // 声明UI对象
    Button bt_login = null;
    EditText et_account = null;
    EditText et_password = null;
    private LinearLayout net;
    private int netMobile;
    private NetBroadcastReceiver netBroadcastReceiver;

    // 声明SharedPreferences对象
    SharedPreferences sp;
    // 声明SharedPreferences编辑器对象
    SharedPreferences.Editor editor;
    // 声明token
    private String token;
    private String token_account;
    private String token_password;

    // Log打印的通用Tag
    private final String TAG = "LoginActivity";


    /*
        为了避免onCreate方法体看起来过于庞大
        把一些代码封装成方法放到onCreate之外了
        比如initUI()、setOnClickListener()等等
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
//            fullScreenConfig();
            //从共享参数获取数据
            sp = getSharedPreferences("login_info", MODE_PRIVATE);
            String account=sp.getString("account","");
            if (account != null && !account.isEmpty()) {
                //若值为true,用户无需输入密码，直接跳转进入操作界面
                Intent intent = new Intent(LoginActivity.this,
                        MainActivity.class);
                startActivity(intent);
            }
            setContentView(R.layout.activity_login);
        }catch (Exception ex){
            Log.i(TAG,ex.getMessage());
        }
        // 初始化UI对象
        initUI();
        // 为点击事件设置监听器
        setOnClickListener();

        /*
            设置当输入框焦点失去时提示错误信息
            第一个参数指明输入框对象
            第二个参数指明输入数据类型
            第三个参数指明输入不合法时提示信息
         */
        setOnFocusChangeErrMsg(et_account, "account", "账号不能为空");
        setOnFocusChangeErrMsg(et_password, "password", "密码不能为空");
    }
    //	进入登录页面就检查
    @Override
    public void onResume() {
        super.onResume();
        if (netBroadcastReceiver == null) {

            try{
//                net.setVisibility(View.VISIBLE);
                //实例化网络接收器
                netBroadcastReceiver = new NetBroadcastReceiver();
                //实例化意图
                IntentFilter filter = new IntentFilter();
                //设置广播的类型
                filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
                //注册广播，有网络变化的时候会触发onReceive
                this.registerReceiver(netBroadcastReceiver, filter);
                // 设置监听
                netBroadcastReceiver.setNetEvent(this);
            }catch (Exception ex){
                Log.i(TAG,ex.getMessage());
            }
        }
    }
    //网络状态变化后，回调此方法，前端页面做出相应改变
    @Override
    public void onNetChange(int netMobile) {
        // TODO Auto-generated method stub
        this.netMobile = netMobile;
        isNetConnect();
    }

    private void isNetConnect() {
        switch (netMobile) {
            case 1:// wifi
                net.setVisibility(View.GONE);
                break;
            case 0:// 移动数据
                net.setVisibility(View.GONE);
                break;
            case -1:// 没有网络
                net.setVisibility(View.VISIBLE);
                break;
        }
    }

    //	在页面销毁的时候，取消注册的广播
    @Override
    public void onPause() {
        super.onPause();
        if (netBroadcastReceiver != null) {
            try{
                this.unregisterReceiver(netBroadcastReceiver);
            }catch (Exception ex){
                Log.i(TAG,ex.getMessage());
            }
        }
    }

    // 全屏显示
    private void fullScreenConfig() {
        // 去除状态栏，如 电量、Wifi信号等
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }


    // 初始化UI对象
    private void initUI() {
        bt_login = findViewById(R.id.bt_login); // 登录按钮
        et_account = findViewById(R.id.et_account); // 输入账号
        et_password = findViewById(R.id.et_password); // 输入密码
         net = (LinearLayout) findViewById(R.id.net);
    }

    /*
    当输入账号FocusChange时，校验账号是否是中国大陆手机号
    当输入密码FocusChange时，校验密码是否不少于6位
     */
    private void setOnFocusChangeErrMsg(final EditText editText, final String inputType, final String errMsg) {
        editText.setOnFocusChangeListener(
                new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        String inputStr = editText.getText().toString();
                        if (!hasFocus) {
                            if (inputType == "account") {
                                if (!isAccountValid(inputStr)) {
                                    editText.setError(errMsg);
                                }
                            }
                            if (inputType == "password") {
                                if (isPasswordValid(inputStr)) {
                                    editText.setError(null);
                                } else {
                                    editText.setError(errMsg);
                                }
                            }
                        }
                    }
                }
        );
    }

    // 校验账号不能为空
    private boolean isAccountValid(String account) {
        if (TextUtils.isEmpty(account)) {
            return false;
        }else{
            return true;
        }
        // 首位为1, 第二位为3-9, 剩下九位为 0-9, 共11位数字
//        String pattern = "^[1]([3-9])[0-9]{9}$";
//        Pattern r = Pattern.compile(pattern);
//        Matcher m = r.matcher(account);
//        return m.matches();
    }

    // 校验密码
    private boolean isPasswordValid(String password) {
        if (TextUtils.isEmpty(password)) {
            return false;
        }else{
            return true;
        }
    }


    // 为点击事件的UI对象设置监听器
    private void setOnClickListener() {
        bt_login.setOnClickListener(this); // 登录按钮
    }

    // 因为 implements View.OnClickListener 所以OnClick方法可以写到onCreate方法外
    @Override
    public void onClick(View v) {
        // 获取用户输入的账号和密码以进行验证
        String account = et_account.getText().toString();
        String password = et_password.getText().toString();

        switch (v.getId()) {
            // 登录按钮 响应事件
            case R.id.bt_login:
                // 让密码输入框失去焦点,触发setOnFocusChangeErrMsg方法
                et_password.clearFocus();
                // 发送URL请求之前,先进行校验
                if (!(isAccountValid(account) && isPasswordValid(password))) {
                    Toast.makeText(this, "账号或密码未填写", Toast.LENGTH_SHORT).show();
                    break;
                }
                /*
                   因为验证是耗时操作，所以独立成方法
                   在方法中开辟子线程，避免在当前UI线程进行耗时操作
                */
                asyncValidate(account, password);
                break;
            // 注册用户 响应事件
            //case R.id.tv_to_register:
                /*
                  关于这里传参说明：给用户一个良好的体验，
                  如果在登录界面填写过的，就不需要再填了
                  所以Intent把填写过的数据传递给注册界面
                 */
//                Intent it_login_to_register = new Intent(this, RegisterActivity.class);
//                it_login_to_register.putExtra("account", account);
//                startActivity(it_login_to_register);
               // break;

        }
    }

    /*
      okhttp异步POST请求 要求API level 21+
      account 本来想的是可以是 telphone或者username
      但目前只实现了telphone
     */
    private void asyncValidate(final String account, final String password) {
        /*
         发送请求属于耗时操作，所以开辟子线程执行
         上面的参数都加上了final，否则无法传递到子线程中
        */
        new Thread(new Runnable() {
            @Override
            public void run() {
                // okhttp异步POST请求； 总共5步
                OkHttpClient.Builder builder=new OkHttpClient.Builder();
                HttpLoggingInterceptor loggingInterceptor= new HttpLoggingInterceptor(message -> Log.e("HttpLoggingInterceptor",message));
                loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                builder.addInterceptor(loggingInterceptor);
                // 1、初始化okhttpClient对象
                OkHttpClient okHttpClient = builder.build();
                // 2、构建请求体requestBody
                RequestBody requestBody = new FormBody.Builder()
                        .add("username", account)
                        .add("password", md5Decode(password))
                        .build();
                // 3、发送请求，因为要传密码，所以用POST方式
                Request request = new Request.Builder()
                        .url(UrlConstant.LoginUrl)
                        .post(requestBody)
                        .build();
                // 4、使用okhttpClient对象获取请求的回调方法，enqueue()方法代表异步执行
                okHttpClient.newCall(request).enqueue(new Callback() {
                    // 5、重写两个回调方法
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.d(TAG, "请求URL失败： " + e.getMessage());
                        showToastInThread(LoginActivity.this, "请求URL失败, 请重试！");
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        // 先判断一下服务器是否异常
                        String responseStr = response.toString();
                        if (responseStr.contains("200")) {
                             /*
                            注意这里，同一个方法内
                            response.body().string()只能调用一次，多次调用会报错
                             */
                            /* 使用Gson解析response的JSON数据的第一步 */
                            String responseBodyStr = response.body().string();
                            Gson gson=new Gson();
                            ReturnData data=gson.fromJson(responseBodyStr,ReturnData.class);
                            /* 使用Gson解析response的JSON数据的第二步 */
                            JsonObject responseBodyJSONObject = (JsonObject) new JsonParser().parse(responseBodyStr);
                            // 如果返回的status为success，则getStatus返回true，登录验证通过
                            if (data.type==1) {
                            /*
                             更新token，下次自动登录
                             真实的token值应该是一个加密字符串
                             我为了让token不为null，就随便传了一个字符串
                             这里的account和password每次都要重写的
                             否则无法实现修改密码
                            */
                                sp = getSharedPreferences("login_info", MODE_PRIVATE);
                                editor = sp.edit();
                                editor.putString("token", "token_value");
                                editor.putString("account", account);
                                editor.putString("password", md5Decode(password));
                                if (editor.commit()) {
                                    try {
                                        Intent it_login_to_main = new Intent(LoginActivity.this, MainActivity.class);
                                        startActivity(it_login_to_main);
                                    }catch (Exception ex){
                                        Log.i(TAG,ex.getMessage());
                                    }
                                    // 登录成功后，登录界面就没必要占据资源了
                                    finish();
                                } else {
                                    showToastInThread(LoginActivity.this, "token保存失败，请重新登录");
                                }
                            } else {
                                showToastInThread(LoginActivity.this, data.message);
                                Log.d(TAG, "账号或密码验证失败");
                            }
                        } else {
                            Log.d(TAG, "服务器异常");
                            showToastInThread(LoginActivity.this, responseStr);
                        }
                    }
                });

            }
        }).start();
    }
    /**
     * 32位MD5加密
     * @param content -- 待加密内容
     * @return
     */
    public String md5Decode(String content) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(content.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("NoSuchAlgorithmException",e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UnsupportedEncodingException", e);
        }
        //对生成的16字节数组进行补零操作
        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10){
                hex.append("0");
            }
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }
    /*
      使用Gson解析response的JSON数据
      本来总共是有三步的，一、二步在方法调用之前执行了
    */
    private String getStatus(JsonObject responseBodyJSONObject) {
        /* 使用Gson解析response的JSON数据的第三步
           通过JSON对象获取对应的属性值 */
        String status = responseBodyJSONObject.get("status").getAsString();
        // 登录成功返回的json为{ "status":"success", "data":null }
        // 只获取status即可，data为null
        return status;
    }

    /*
      使用Gson解析response返回异常信息的JSON中的data对象
      这也属于第三步，一、二步在方法调用之前执行了
     */
    private void getResponseErrMsg(Context context, JsonObject responseBodyJSONObject) {
        JsonObject dataObject = responseBodyJSONObject.get("data").getAsJsonObject();
        String errorCode = dataObject.get("errorCode").getAsString();
        String errorMsg = dataObject.get("errorMsg").getAsString();
        Log.d(TAG, "errorCode: " + errorCode + " errorMsg: " + errorMsg);
        // 在子线程中显示Toast
        showToastInThread(context, errorMsg);
    }

    // 实现在子线程中显示Toast
    private void showToastInThread(final Context context, final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

}
