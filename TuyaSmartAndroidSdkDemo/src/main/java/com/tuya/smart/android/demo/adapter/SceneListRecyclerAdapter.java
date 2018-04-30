package com.tuya.smart.android.demo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.bean.SceneListItemBean;
import com.tuya.smart.sdk.TuyaScene;
import com.tuya.smart.sdk.api.scene.IExecuteSceneCallback;

import java.util.List;

/**
 * 智能场景adapter
 * Created by HanZheng(305058709@qq.com) on 2018-1-22.
 */

public class SceneListRecyclerAdapter extends RecyclerView.Adapter<SceneListRecyclerAdapter
        .SceneListItemViewHolder> {
    static final String TAG = SceneListRecyclerAdapter.class.getSimpleName();
    List<SceneListItemBean> mList;
    Context mContext;

    public SceneListRecyclerAdapter(Context context, List<SceneListItemBean> list) {
        mList = list;
        mContext = context;
    }

    public SceneListItemBean getData(int i) {
        return mList.get(i);
    }

    public void setData(List<SceneListItemBean> list) {
        mList = list;
        notifyDataSetChanged();
    }

    @Override
    public SceneListItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new SceneListItemViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R
                .layout.recycler_scene_list_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(SceneListItemViewHolder sceneListItemViewHolder, int i) {
        final SceneListItemBean item = mList.get(i);
        sceneListItemViewHolder.sceneName.setText(item.getName());
        sceneListItemViewHolder.icon.setImageResource(item.icon);
        //预置场景
        if (TextUtils.isEmpty(item.getId())) {
            sceneListItemViewHolder.btnExecute.setBackgroundResource(R.drawable
                    .shape_scene_execute_disable);
            sceneListItemViewHolder.btnExecute.setTextColor(mContext.getColor(R.color.gray_30));
        } else {
            sceneListItemViewHolder.btnExecute.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TuyaScene.getTuyaSmartScene(item.getId()).executeScene(new IExecuteSceneCallback() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "onSuccess");
                            Toast.makeText(mContext, R.string.alert_scene_execute, Toast
                                    .LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(String s, String s1) {
                            Log.d(TAG, s + s1);
                        }
                    });
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class SceneListItemViewHolder extends RecyclerView.ViewHolder {
        public ImageView icon;
        public TextView sceneName;
        public TextView btnExecute;

        public SceneListItemViewHolder(View itemView) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.iv_scene_icon);
            sceneName = (TextView) itemView.findViewById(R.id.tv_icon_text);
            btnExecute = (TextView) itemView.findViewById(R.id.tv_scene_function);
        }
    }
}
