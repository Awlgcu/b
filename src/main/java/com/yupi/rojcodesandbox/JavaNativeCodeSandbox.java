package com.yupi.rojcodesandbox;

import com.yupi.rojcodesandbox.model.ExecuteCodeRequest;
import com.yupi.rojcodesandbox.model.ExecuteCodeResponse;
import org.springframework.stereotype.Component;

/**
 * javaNative代码沙箱,直接复用模板的方法即可,因为模板就是根据原生代码改造的.
 */
@Component
public class JavaNativeCodeSandbox extends JavaCodeSandboxTemplate {
    @Override
    public ExecuteCodeResponse ExecuteCode(ExecuteCodeRequest executeCodeRequest) {
        return super.ExecuteCode(executeCodeRequest);
    }
}
