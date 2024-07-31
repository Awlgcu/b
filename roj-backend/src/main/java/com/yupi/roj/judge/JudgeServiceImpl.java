package com.yupi.roj.judge;

import cn.hutool.json.JSONUtil;
import com.yupi.roj.common.ErrorCode;
import com.yupi.roj.exception.BusinessException;
import com.yupi.roj.judge.codesandbox.CodeSandbox;
import com.yupi.roj.judge.codesandbox.CodeSandboxFactory;
import com.yupi.roj.judge.codesandbox.CodeSandboxProxy;
import com.yupi.roj.judge.codesandbox.model.ExecuteCodeRequest;
import com.yupi.roj.judge.codesandbox.model.ExecuteCodeResponse;
import com.yupi.roj.judge.strategy.JudgeContext;
import com.yupi.roj.model.dto.question.JudgeCase;
import com.yupi.roj.judge.codesandbox.model.JudgeInfo;
import com.yupi.roj.model.entity.Question;
import com.yupi.roj.model.entity.QuestionSubmit;
import com.yupi.roj.model.enums.QuestionSubmitStatusEnum;
import com.yupi.roj.service.QuestionService;
import com.yupi.roj.service.QuestionSubmitService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JudgeServiceImpl implements JudgeService{
    @Resource
    private QuestionService questionService;
    @Resource
    private JudgeManager judgeManager;
    @Resource
    private QuestionSubmitService questionSubmitService;
    @Value("${codesandbox.type:example}")//:thirdparty是默认值
    private String type;
    @Override
    public QuestionSubmit doJudge(long questionSubmitId) {
        //1.根据传入的提交题目id,拿到题目信息和提交的代码
        QuestionSubmit questionSubmit = questionSubmitService.getById(questionSubmitId);
        if(questionSubmit==null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "提交信息不存在");
        }
        Long questionId = questionSubmit.getQuestionId();
        Question question = questionService.getById(questionId);
        if(question==null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        }
        //2.先校验提交状态,如果不是等待中(等待判题)则不用进行判题了,防止重复执行
        if(!questionSubmit.getStatus().equals(QuestionSubmitStatusEnum.WAITING.getValue())){
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "题目正在判题");
        }
        //3.进入判题状态,则更改一下提交状态为判题中.也是防止重复执行
        QuestionSubmit questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.RUNNING.getValue());
        boolean res = questionSubmitService.updateById(questionSubmitUpdate);
        if(!res){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目更新错误");
        }
        //4.开始判题,调用沙箱,执行代码
        //4.1先拿到判题所需的信息,将其封装成执行代码沙箱的请求ExecuteCodeRequest
        String language = questionSubmit.getLanguage();
        String code = questionSubmit.getCode();
        String judgeCaseStr = question.getJudgeCase();
        List<JudgeCase> judgeCaseList = JSONUtil.toList(judgeCaseStr, JudgeCase.class);
        List<String> inputList = judgeCaseList.stream().map(JudgeCase::getInput).collect(Collectors.toList());
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .code(code)
                .language(language)
                .inputList(inputList)
                .build();
        //创建代码沙箱并执行,得到返回结果
        CodeSandbox codeSandbox = CodeSandboxFactory.newInstance(type);
        CodeSandboxProxy codeSandboxProxy = new CodeSandboxProxy(codeSandbox);
        ExecuteCodeResponse executeCodeResponse = codeSandboxProxy.ExecuteCode(executeCodeRequest);
        JudgeInfo judgeInfo = executeCodeResponse.getJudgeInfo();
        List<String> outputList = executeCodeResponse.getOutputList();
        //5.根据沙箱执行的结果正确与否,去修改判题状态,专门定义了一个接口去实现判断
        //5.1先得到判题上下文,即判题所需的信息
        JudgeContext judgeContext = new JudgeContext();
        judgeContext.setJudgeInfo(judgeInfo);
        judgeContext.setInputList(inputList);
        judgeContext.setOutputList(outputList);
        judgeContext.setJudgeCaseList(judgeCaseList);
        judgeContext.setQuestion(question);
        judgeContext.setQuestionSubmit(questionSubmit);

        //5.2根据编程语言选择判题策略
//        JudgeStrategy judgeStrategy = new DefaultJudgeStrategy();
        JudgeInfo judgeInfoResponse = judgeManager.doJudge(judgeContext);

        //根据判题反馈修改数据库中的判题状态
        questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());
        questionSubmitUpdate.setJudgeInfo(JSONUtil.toJsonStr(judgeInfoResponse));
        res = questionSubmitService.updateById(questionSubmitUpdate);
        if(!res){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目更新错误");
        }
        //6.根据沙箱的返回信息得到封装类,并返回.
        QuestionSubmit questionSubmitRt = questionSubmitService.getById(questionId);
        return questionSubmitRt;
    }
}
