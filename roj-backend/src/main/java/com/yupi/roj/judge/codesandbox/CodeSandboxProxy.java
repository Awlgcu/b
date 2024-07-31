package com.yupi.roj.judge.codesandbox;

import com.yupi.roj.judge.codesandbox.model.ExecuteCodeRequest;
import com.yupi.roj.judge.codesandbox.model.ExecuteCodeResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CodeSandboxProxy implements CodeSandbox {
    private CodeSandbox codeSandbox;
    public CodeSandboxProxy(CodeSandbox codeSandbox){
        this.codeSandbox = codeSandbox;
    }
    @Override
    public ExecuteCodeResponse ExecuteCode(ExecuteCodeRequest executeCodeRequest) {
        log.info("代码沙箱请求信息:"+executeCodeRequest);
        ExecuteCodeResponse executeCodeResponse = codeSandbox.ExecuteCode(executeCodeRequest);
        log.info("代码沙箱响应信息:"+executeCodeResponse);
        return executeCodeResponse;
    }
}
