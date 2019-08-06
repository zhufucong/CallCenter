package com.hzandriod.callcenter.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import android.content.Intent;
import android.content.IntentFilter;

import com.hzandriod.callcenter.bordercast.CallReceiver;
import com.hzandriod.callcenter.R;
import com.hzandriod.callcenter.activity.MainFragment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

/**
 * 主Activity
 *
 * @author wwj_748
 *
 */
public class MainActivity extends AppCompatActivity implements OnClickListener {
    public static MainActivity mactivity;
    CallReceiver callReceiver;
    // 三个tab布局
    private RelativeLayout knowLayout, iWantKnowLayout, meLayout;

    // 底部标签切换的Fragment
    private Fragment mainFragment, meFragment,currentFragment;
    // 底部标签图片
    private ImageView deskImg, meImg;
    // 底部标签的文本
    private TextView deskTv, meTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mactivity=this;
        IntentFilter intentFilter=new IntentFilter(Intent.ACTION_NEW_OUTGOING_CALL);
        intentFilter.setPriority(Integer.MAX_VALUE);
        registerReceiver(callReceiver,intentFilter);

        initUI();
        initTab();
    }

    /**
     * 初始化UI
     */
    private void initUI() {
        knowLayout = (RelativeLayout) findViewById(R.id.rl_main);
        meLayout = (RelativeLayout) findViewById(R.id.rl_me);
        knowLayout.setOnClickListener(this);
        meLayout.setOnClickListener(this);

        deskImg = (ImageView) findViewById(R.id.iv_desktop);
        deskTv= (TextView) findViewById(R.id.tv_desktop);
        meImg = (ImageView) findViewById(R.id.iv_me);
        meTv = (TextView) findViewById(R.id.tv_me);

    }

    /**
     * 初始化底部标签
     */
    private void initTab() {
        if (mainFragment == null) {
            mainFragment = new MainFragment();
        }

        if (!mainFragment.isAdded()) {
            // 提交事务
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content_layout, mainFragment).commit();

            // 记录当前Fragment
            currentFragment = mainFragment;
            // 设置图片文本的变化
            deskImg.setImageResource(R.mipmap.workdesk_pres);
            deskTv.setTextColor(getResources().getColor(R.color.bottomtab_press));
            meImg.setImageResource(R.mipmap.me);
            meTv.setTextColor(getResources().getColor(R.color.bottomtab_normal));

        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_main: // 工作台
                clickTab1Layout();
                break;
            case R.id.rl_me: // 我的
                clickTab3Layout();
                break;
            default:
                break;
        }
    }

    /**
     * 点击第一个tab
     */
    private void clickTab1Layout() {
        if (mainFragment == null) {
            mainFragment = new MainFragment();
        }
        addOrShowFragment(getSupportFragmentManager().beginTransaction(), mainFragment);

        // 设置底部tab变化
        deskImg.setImageResource(R.mipmap.workdesk_pres);
        deskTv.setTextColor(getResources().getColor(R.color.bottomtab_press));
        meImg.setImageResource(R.mipmap.me);
        meTv.setTextColor(getResources().getColor(R.color.bottomtab_normal));
    }

    /**
     * 点击第三个tab
     */
    private void clickTab3Layout() {
        try{
            if (meFragment == null) {
                meFragment = new MeFragment();
            }

            addOrShowFragment(getSupportFragmentManager().beginTransaction(), meFragment);
            // 设置底部tab变化
            deskImg.setImageResource(R.mipmap.workdesk);
            deskTv.setTextColor(getResources().getColor(R.color.bottomtab_normal));
            meImg.setImageResource(R.mipmap.me_pres);
            meTv.setTextColor(getResources().getColor(R.color.bottomtab_press));
        }catch (Exception ex){
            Log.i("MainActivity",ex.getMessage());
        }

    }

    /**
     * 添加或者显示碎片
     *
     * @param transaction
     * @param fragment
     */
    private void addOrShowFragment(FragmentTransaction transaction,
                                   Fragment fragment) {
        if (currentFragment == fragment)
            return;

        if (!fragment.isAdded()) { // 如果当前fragment未被添加，则添加到Fragment管理器中
            transaction.hide(currentFragment)
                    .add(R.id.content_layout, fragment).commit();
        } else {
            transaction.hide(currentFragment).show(fragment).commit();
        }

        currentFragment = fragment;
    }

}

