package me.wizos.loread.net;


import com.lzy.okgo.request.base.Request;

/**
 * 该类作为网络请求时的各种回调接口
 * Created by Wizos on 2017/10/2.
 */
/*
Code 	Description
200 	Request OK
400 	Mandatory parameter(s) missing
401 	End-user not authorized
403 	You are not sending the correct AppID and/or AppSecret
404 	Method not implemented
429 	Daily limit reached for this zone
503 	Service unavailable
 */
public abstract class NetCallbackS {

    public void onSuccess(String body) {
    }

    public void onFailure(Request request) {
    }
//
//    public void onSuccess( Response response ) {
//        if(response.code()!=200){
//            ToastUtil.showLong("返回不是Success");
//            onFailure(response);
//        }
//    }

//    public void onFailure( Response response ) {
//    }
}
