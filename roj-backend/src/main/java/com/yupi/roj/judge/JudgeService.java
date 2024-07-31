package com.yupi.roj.judge;

import com.yupi.roj.model.entity.QuestionSubmit;
import com.yupi.roj.model.vo.QuestionSubmitVO;
import org.springframework.stereotype.Service;

@Service
public interface JudgeService {
    QuestionSubmit doJudge(long questionSubmitId);
}
