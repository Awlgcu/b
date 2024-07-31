package com.yupi.roj.judge.codesandbox;

import com.yupi.roj.judge.codesandbox.impl.RemoteCodeSandbox;
import com.yupi.roj.judge.codesandbox.model.ExecuteCodeRequest;
import com.yupi.roj.judge.codesandbox.model.ExecuteCodeResponse;
import com.yupi.roj.model.enums.QuestionSubmitLanguageEnum;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
class codeSandboxTest {
    @Value("${codesandbox.type:remote}")//:thirdparty是默认值
    private String type;

    @Test
    void executeCode() {
        CodeSandbox codeSandbox = CodeSandboxFactory.newInstance(type);
        String code = "public class Main {\n" +
                "    public static void main(String[] args) {\n" +
                "        int a = Integer.parseInt(args[0]);\n" +
                "        int b = Integer.parseInt(args[1]);\n" +
                "        System.out.println(a+b);\n" +
                "    }\n" +
                "}";
        String language = QuestionSubmitLanguageEnum.JAVA.getValue();
        List<String> inputList = Arrays.asList("1 2", "3 4");
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .code(code)
                .language(language)
                .inputList(inputList)
                .build();
        CodeSandboxProxy codeSandboxProxy = new CodeSandboxProxy(codeSandbox);
        ExecuteCodeResponse executeCodeResponse = codeSandboxProxy.ExecuteCode(executeCodeRequest);

//        RemoteCodeSandbox remoteCodeSandbox = new RemoteCodeSandbox();
//        ExecuteCodeResponse executeCodeResponse = remoteCodeSandbox.ExecuteCode(executeCodeRequest);
//        Assertions.assertNotNull(executeCodeResponse);
    }
}