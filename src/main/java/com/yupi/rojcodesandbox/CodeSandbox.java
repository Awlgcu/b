package com.yupi.rojcodesandbox;

import com.yupi.rojcodesandbox.model.ExecuteCodeRequest;
import com.yupi.rojcodesandbox.model.ExecuteCodeResponse;

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
