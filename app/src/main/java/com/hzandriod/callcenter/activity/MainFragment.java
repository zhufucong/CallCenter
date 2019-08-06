package com.hzandriod.callcenter.activity;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.SimpleAdapter;

import com.hzandriod.callcenter.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainFragment extends Fragment {


    public static MainFragment newInstance() {
        return new MainFragment();
    }
    private GridView gview;
    private List<Map<String, Object>> data_list;
    private SimpleAdapter sim_adapter;
    private GridView top_gview;
    private List<Map<String, Object>> top_data_list;
    private SimpleAdapter top_sim_adapter;
    // 图片封装为一个数组
    private int[] icon = { R.mipmap.allcustomer, R.mipmap.mycustomer,R.mipmap.genjinjilu,R.mipmap.callrecord};
    private String[] iconName = { "全部客户", "我的客户", "跟进记录", "通话记录" };
    private int[] top_icon = { R.mipmap.addcus, R.mipmap.tracerecord};
    private String[] top_iconName = { "添加客户","添加跟进" };
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_fragment, container, false);
        initMenu1(view);
        initMenu2(view);
        return view;
    }
    private void initMenu1(View view){
        gview = (GridView) view.findViewById(R.id.GridMenu);
        //新建List
        data_list = new ArrayList<Map<String, Object>>();
        //获取数据
        getData();
        //新建适配器
        String [] from ={"Menuimage","Menutext"};
        int [] to = {R.id.Menuimage,R.id.Menutext};
        sim_adapter = new SimpleAdapter(getContext(), data_list,R.layout.item , from, to);
        //配置适配器
        gview.setAdapter(sim_adapter);
    }
    private void initMenu2(View view){
        top_gview = (GridView) view.findViewById(R.id.GridMenu_top);
        //新建List
        top_data_list = new ArrayList<Map<String, Object>>();
        //获取数据
        getData_top();
        //新建适配器
        String [] from ={"top_Menuimage","top_Menutext"};
        int [] to = {R.id.top_Menuimage,R.id.top_Menutext};
        top_sim_adapter = new SimpleAdapter(getContext(), top_data_list,R.layout.item_top , from, to);
        //配置适配器
        top_gview.setAdapter(top_sim_adapter);
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        mViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        // TODO: Use the ViewModel
    }
    public List<Map<String, Object>> getData(){
        //cion和iconName的长度是相同的，这里任选其一都可以
        for(int i=0;i<icon.length;i++){
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("Menuimage", icon[i]);
            map.put("Menutext", iconName[i]);
            data_list.add(map);
        }

        return data_list;
    }
    public List<Map<String, Object>> getData_top(){
        //cion和iconName的长度是相同的，这里任选其一都可以
        for(int i=0;i<top_icon.length;i++){
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("top_Menuimage", top_icon[i]);
            map.put("top_Menutext", top_iconName[i]);
            top_data_list.add(map);
        }

        return top_data_list;
    }

}
