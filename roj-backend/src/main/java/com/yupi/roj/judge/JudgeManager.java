package com.yupi.roj.judge;

import com.yupi.roj.judge.strategy.DefaultJudgeStrategy;
import com.yupi.roj.judge.strategy.JavaLanguageJudgeStrategy;
import com.yupi.roj.judge.strategy.JudgeContext;
import com.yupi.roj.judge.strategy.JudgeStrategy;
import com.yupi.roj.judge.codesandbox.model.JudgeInfo;
import com.yupi.roj.model.entity.QuestionSubmit;
import org.springframework.stereotype.Service;

@Service
public class JudgeManager {
    /**
     * sadda
     * 为了不在业务中使用if判断来选择判题策略,写了一个类来封装选策略的过程!
     * @param judgeContext
     * @return
     */

    JudgeInfo doJudge(JudgeContext judgeContext){
        QuestionSubmit questionSubmit = judgeContext.getQuestionSubmit();
        String language = questionSubmit.getLanguage();
        JudgeStrategy judgeStrategy = new DefaultJudgeStrategy();
        if("java".equals(language)){
            judgeStrategy = new JavaLanguageJudgeStrategy();
        }
        return judgeStrategy.doJudge(judgeContext);
    }
}
