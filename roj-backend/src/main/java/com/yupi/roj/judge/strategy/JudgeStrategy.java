package com.yupi.roj.judge.strategy;

import com.yupi.roj.judge.codesandbox.model.JudgeInfo;

/**
 * 基于策略模式来对不同的语言执行不同的判题信息
 */
public interface JudgeStrategy {
    /**
     * 实现判题策略的接口
     * @param judgeContext
     * @return
     */

    JudgeInfo doJudge(JudgeContext judgeContext);
}
