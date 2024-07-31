package com.yupi.roj.judge.codesandbox.impl;

import com.yupi.roj.judge.codesandbox.CodeSandbox;
import com.yupi.roj.judge.codesandbox.model.ExecuteCodeRequest;
import com.yupi.roj.judge.codesandbox.model.ExecuteCodeResponse;

/**
 * 可尝试调用第三方代码沙箱的接口
 */
public class ThirdPartyCodeSandbox implements CodeSandbox {

    @Override
    public ExecuteCodeResponse ExecuteCode(ExecuteCodeRequest executeCodeRequest) {
        System.out.println("第三方代码沙箱");
        return null;
    }
}
