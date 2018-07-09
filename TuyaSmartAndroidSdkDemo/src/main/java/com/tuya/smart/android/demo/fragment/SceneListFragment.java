package com.tuya.smart.android.demo.fragment;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.tuya.smart.android.common.utils.NetworkUtil;
import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.activity.AddSceneActivity;
import com.tuya.smart.android.demo.adapter.DividerItemDecoration;
import com.tuya.smart.android.demo.adapter.ItemClickSupport;
import com.tuya.smart.android.demo.adapter.SceneListRecyclerAdapter;
import com.tuya.smart.android.demo.bean.SceneListItemBean;
import com.tuya.smart.sdk.TuyaScene;
import com.tuya.smart.sdk.api.ITuyaDataCallback;
import com.tuya.smart.sdk.bean.scene.SceneBean;

import java.util.List;


/**
 * 智能场景
 * A simple {@link Fragment} subclass.
 */
public class SceneListFragment extends BaseFragment {
    static final int REQUEST_ADD_SCENE = 101;
    static final String TAG = SceneListFragment.class.getSimpleName();
    volatile static SceneListFragment instance;
    SceneListRecyclerAdapter adapter;
    SwipeRefreshLayout mSwipeRefreshLayout;
    View mContentView;
    RecyclerView mRecyclerView;

    public static Fragment newInstance() {
        if (instance == null) {
            synchronized (SceneListFragment.class) {
                if (instance == null) {
                    instance = new SceneListFragment();
                }
            }
        }
        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.fragment_scene_list, container, false);
        initToolbar(mContentView);
        initMenu();
        return mContentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initViews();
        getScnenList();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ADD_SCENE) {
            if (resultCode == Activity.RESULT_OK) {
                //刷新界面
                getScnenList();
            }
        }
    }

    protected void initMenu() {
        setTitle(getString(R.string.home_scene));
        setMenu(R.menu.toolbar_add_scene, new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_add_scene) {
                    Intent intent = new Intent(getActivity(), AddSceneActivity.class);
                    //startActivity(intent);
                    startActivityForResult(intent, REQUEST_ADD_SCENE);
                }
                return false;
            }
        });
    }

    protected void initViews() {
        mRecyclerView = (RecyclerView) mContentView.findViewById(R.id.scene_list_recyclerView);
        mSwipeRefreshLayout = (SwipeRefreshLayout) mContentView.findViewById(R.id
                .scene_list_swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (NetworkUtil.isNetworkAvailable(getContext())) {
                    //mDeviceListFragmentPresenter.getDataFromServer();
                    getScnenList();
                } else {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }
        });
    }

    //    protected void initSceneList() {
//        adapter = new SceneListRecyclerAdapter(SceneListItemBean.getList(getActivity()
//                .getApplicationContext()));
//        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
//        mRecyclerView.setLayoutManager(manager);
//        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),
//                DividerItemDecoration.VERTICAL_LIST));
//        mRecyclerView.setAdapter(adapter);
//    }
    protected void bindSceneList(List<SceneListItemBean> list) {
        adapter = new SceneListRecyclerAdapter(getActivity(), list);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL_LIST));
        mRecyclerView.setAdapter(adapter);
        ItemClickSupport.addTo(mRecyclerView).setOnItemClickListener(new ItemClickSupport
                .OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                SceneListRecyclerAdapter adapter = (SceneListRecyclerAdapter) recyclerView
                        .getAdapter();
                SceneListItemBean bean = adapter.getData(position);
                Intent intent = new Intent(getActivity(), AddSceneActivity.class);
                intent.putExtra(AddSceneActivity.INTENT_SCENEBEAN, bean);
                startActivityForResult(intent, REQUEST_ADD_SCENE);
            }
        });
    }

    protected void getScnenList() {

        TuyaScene.getTuyaSceneManager().getSceneList(new ITuyaDataCallback<List<SceneBean>>() {
            @Override
            public void onSuccess(List<SceneBean> result) {
                Log.d(TAG, "onSuccess");
                List<SceneListItemBean> list = SceneListItemBean.addAll(getActivity(), result);
                for (SceneBean bean : result) {
                    Log.d(TAG, bean.getName() + "," + bean.getId() + "," + bean.getCode());

                }
                mSwipeRefreshLayout.setRefreshing(false);
                bindSceneList(list);
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                Log.d(TAG, "onError" + errorCode + "," + errorMessage);
            }
        });
    }
}
