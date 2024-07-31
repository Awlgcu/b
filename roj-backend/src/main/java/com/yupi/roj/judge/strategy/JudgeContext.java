package com.yupi.roj.judge.strategy;

import com.yupi.roj.model.dto.question.JudgeCase;
import com.yupi.roj.judge.codesandbox.model.JudgeInfo;
import com.yupi.roj.model.entity.Question;
import com.yupi.roj.model.entity.QuestionSubmit;
import lombok.Data;

import java.util.List;

/**
 *判题上下文,用于策略模式中传递判题参数
 */
@Data
public class JudgeContext {
    private JudgeInfo judgeInfo;
    private List<String> inputList;
    private List<String> outputList;
    private List<JudgeCase> judgeCaseList;
    private Question question;
    private QuestionSubmit questionSubmit;
}
