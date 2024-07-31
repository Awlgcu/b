package com.yupi.roj.judge.codesandbox.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.yupi.roj.common.ErrorCode;
import com.yupi.roj.exception.BusinessException;
import com.yupi.roj.judge.codesandbox.CodeSandbox;
import com.yupi.roj.judge.codesandbox.model.ExecuteCodeRequest;
import com.yupi.roj.judge.codesandbox.model.ExecuteCodeResponse;
import org.springframework.stereotype.Component;

/**
 * 要实现的我们自己调用docker的接口
 */
public class RemoteCodeSandbox implements CodeSandbox {
    private String AUTH_REQUEST_HEADER = "auth";
    private String AUTH_REQUEST_SECRET = "secretKey";

    @Override
    public ExecuteCodeResponse ExecuteCode(ExecuteCodeRequest executeCodeRequest) {
        System.out.println("使用远程代码沙箱");
        String url = "http://localhost:8095/executeCode";
        //将请求参数转化为jsonstr
        String jsonStr = JSONUtil.toJsonStr(executeCodeRequest);
        //利用httpytil创建一个post请求
        String responseBody = HttpUtil.createPost(url)
                .header(AUTH_REQUEST_HEADER, AUTH_REQUEST_SECRET)//给请求自定义一个请求头,用于鉴权
                .body(jsonStr)//post请求的请求体
                .execute()//执行这个post请求
                .body();//并且拿到结果
//        System.out.println("得到的结果为+"+responseBody);

        if(StrUtil.isBlank(responseBody)){
            throw new BusinessException(ErrorCode.API_REQUEST_ERROR, "request remote codeSandbox error, message:"+responseBody);
        }
        //将拿到的string转化为bean
        return JSONUtil.toBean(responseBody, ExecuteCodeResponse.class);
    }
}
