package com.yupi.roj.judge.codesandbox;

import com.yupi.roj.judge.codesandbox.model.ExecuteCodeRequest;
import com.yupi.roj.judge.codesandbox.model.ExecuteCodeResponse;

/**
 * 代码沙箱接口
 */
public interface CodeSandbox {
    /**
     * 执行代码
     * @param executeCodeRequest
     * @return
     */
    ExecuteCodeResponse ExecuteCode(ExecuteCodeRequest executeCodeRequest);
}
