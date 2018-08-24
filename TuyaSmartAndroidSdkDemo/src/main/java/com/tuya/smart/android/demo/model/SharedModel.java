package com.tuya.smart.android.demo.model;


import android.content.Context;
import android.os.Message;

import com.tuya.smart.android.common.utils.SafeHandler;
import com.tuya.smart.android.demo.config.CommonConfig;
import com.tuya.smart.android.mvp.model.BaseModel;
import com.tuya.smart.android.user.TuyaSmartUserManager;
import com.tuya.smart.android.user.bean.GroupReceivedMemberBean;
import com.tuya.smart.android.user.bean.PersonBean;
import com.tuya.smart.home.interior.presenter.TuyaHomeMember;
import com.tuya.smart.home.sdk.bean.MemberBean;
import com.tuya.smart.home.sdk.callback.ITuyaGetMemberListCallback;
import com.tuya.smart.home.sdk.callback.ITuyaMemberResultCallback;
import com.tuya.smart.sdk.api.IResultCallback;
import com.tuya.smart.sdk.api.share.IAddMemberCallback;
import com.tuya.smart.sdk.api.share.IModifyMemberNameCallback;
import com.tuya.smart.sdk.api.share.IQueryMemberListCallback;
import com.tuya.smart.sdk.api.share.IQueryReceiveMemberListCallback;
import com.tuya.smart.sdk.api.share.IRemoveMemberCallback;
import com.tuya.smart.sdk.api.share.ITuyaMember;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by leaf on 15/12/21.
 * 共享数据
 */
public class SharedModel extends BaseModel implements ISharedModel {
    public static final int WHAT_ERROR = -30;
    public static final int WHAT_ADD_SENT_SUCCESS = 0x030;
    public static final int WHAT_REMOVE_SENT_SUCCESS = 0x031;
    public static final int WHAT_GET_SENT_LIST_SUCCESS = 0x032;
    public static final int WHAT_GET_RECEIVED_LIST_SUCCESS = 0x033;
    public static final int WHAT_UPDATE_NICKNAME_SUCCESS = 0x034;

    //private ITuyaMember mITuyaSmartMember;
    long mHomeId;
    public SharedModel(Context ctx, SafeHandler handler) {
        super(ctx, handler);
        mHomeId = CommonConfig.getHomeId(ctx.getApplicationContext());
    }

    @Override
    public void addMember(String mobile, String name, String countryCode, String relation) {
        TuyaHomeMember.getInstance().addMember(mHomeId, countryCode, mobile, name, true, new ITuyaMemberResultCallback() {
            @Override
            public void onSuccess(MemberBean memberBean) {
                //hanzheng to do 此处long传值可能错误
                resultSuccess(WHAT_ADD_SENT_SUCCESS, memberBean.getMemberId());
            }

            @Override
            public void onError(String s, String s1) {
                resultError(WHAT_ERROR, s, s1);
            }
        });
//        mITuyaSmartMember.addMember(countryCode, mobile, name, relation, new IAddMemberCallback() {
//
//            @Override
//            public void onSuccess(Long aLong) {
//                resultSuccess(WHAT_ADD_SENT_SUCCESS, aLong);
//            }
//
//            @Override
//            public void onError(String errorCode, String errorMsg) {
//                resultError(WHAT_ERROR, errorCode, errorMsg);
//            }
//        });
    }

    @Override
    public void removeMember(String id) {
        long intId = Long.parseLong(id);
        TuyaHomeMember.getInstance().removeMember(intId, new IResultCallback() {
            @Override
            public void onError(String s, String s1) {
                resultError(WHAT_ERROR, s, s1);
            }

            @Override
            public void onSuccess() {
                resultSuccess(WHAT_REMOVE_SENT_SUCCESS);
            }
        });
    }

    @Override
    public void getSentList() {
        TuyaHomeMember.getInstance().queryMemberList(mHomeId, new ITuyaGetMemberListCallback() {
            @Override
            public void onSuccess(List<MemberBean> list) {
                resultSuccess(WHAT_GET_SENT_LIST_SUCCESS, convert(list));
            }

            @Override
            public void onError(String s, String s1) {
                resultError(WHAT_ERROR, s, s1);
            }
        });
//        mITuyaSmartMember.queryMemberList(new IQueryMemberListCallback() {
//            @Override
//            public void onSuccess(ArrayList<PersonBean> arrayList) {
//                resultSuccess(WHAT_GET_SENT_LIST_SUCCESS, arrayList);
//            }
//
//            @Override
//            public void onError(String errorCode, String errorMsg) {
//                resultError(WHAT_ERROR, errorCode, errorMsg);
//            }
//        });
    }

    @Override
    public void getReceivedList() {
        TuyaHomeMember.getInstance().queryMemberList(mHomeId, new ITuyaGetMemberListCallback() {
            @Override
            public void onSuccess(List<MemberBean> list) {
                resultSuccess(WHAT_GET_RECEIVED_LIST_SUCCESS, convert(list));
            }

            @Override
            public void onError(String s, String s1) {
                resultError(WHAT_ERROR, s, s1);
            }
        });
//        mITuyaSmartMember.queryReceiveMemberList(new IQueryReceiveMemberListCallback() {
//            @Override
//            public void onError(String errorCode, String errorMsg) {
//                resultError(WHAT_ERROR, errorCode, errorMsg);
//            }
//
//            @Override
//            public void onSuccess(ArrayList<GroupReceivedMemberBean> arrayList) {
//                resultSuccess(WHAT_GET_RECEIVED_LIST_SUCCESS, arrayList);
//            }
//        });
    }

    @Override
    public void updateMName(long id, String mname) {
        TuyaHomeMember.getInstance().updateMember(id, mname, true, new IResultCallback() {
            @Override
            public void onError(String s, String s1) {
                resultError(WHAT_ERROR, s, s1);
            }

            @Override
            public void onSuccess() {
                resultSuccess(WHAT_UPDATE_NICKNAME_SUCCESS);
            }
        });

//        mITuyaSmartMember.modifyReceiveMemberName(id, mname, new IModifyMemberNameCallback() {
//            @Override
//            public void onSuccess() {
//                resultSuccess(WHAT_UPDATE_NICKNAME_SUCCESS);
//            }
//
//            @Override
//            public void onError(String errorCode, String errorMsg) {
//                resultError(WHAT_ERROR, errorCode, errorMsg);
//            }
//        });
    }


    @Override
    public void onDestroy() {
//        mITuyaSmartMember.onDestroy();
    }

    protected void resultSuccess(int what) {
        if (mHandler != null) {
            Message message = mHandler.obtainMessage(what);
            mHandler.sendMessage(message);
        }
    }
    protected List<PersonBean> convert(List<MemberBean> list){
        List<PersonBean> ret = new ArrayList<>();
        if(list !=null){
            for(MemberBean bean : list){
                ret.add(convert(bean));
            }
        }
        return  ret;
    }
    protected PersonBean convert(MemberBean bean){
        PersonBean ret = new PersonBean();
        ret.setId(bean.getMemberId());
        ret.setMname(bean.getNickName());
        ret.setUsername(bean.getNickName());
        ret.setUid(bean.getUid());
        ret.setMobile(bean.getAccount());
        return ret;
    }
}
