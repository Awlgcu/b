package com.yupi.roj.judge.codesandbox.impl;

import com.yupi.roj.judge.codesandbox.CodeSandbox;
import com.yupi.roj.judge.codesandbox.model.ExecuteCodeRequest;
import com.yupi.roj.judge.codesandbox.model.ExecuteCodeResponse;
import com.yupi.roj.judge.codesandbox.model.JudgeInfo;
import com.yupi.roj.model.enums.JudgeInfoMessageEnum;
import com.yupi.roj.model.enums.QuestionSubmitStatusEnum;

import java.util.Arrays;

/**
 * 示例代码沙箱,检测程序是否能跑通
 */
public class ExampleCodeSandbox implements CodeSandbox {

    @Override
    public ExecuteCodeResponse ExecuteCode(ExecuteCodeRequest executeCodeRequest) {
        System.out.println("示例代码沙箱运行中");
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setOutputList(Arrays.asList("3", "7"));
        executeCodeResponse.setMessage("运行成功");
        executeCodeResponse.setStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());

        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setMessage(JudgeInfoMessageEnum.ACCEPTED.getText());
        judgeInfo.setMemory(100L);
        judgeInfo.setTime(100L);

        executeCodeResponse.setJudgeInfo(judgeInfo);

        return executeCodeResponse;
    }
}
