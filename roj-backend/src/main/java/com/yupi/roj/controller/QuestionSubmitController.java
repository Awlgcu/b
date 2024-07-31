package com.yupi.roj.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.roj.annotation.AuthCheck;
import com.yupi.roj.common.BaseResponse;
import com.yupi.roj.common.ErrorCode;
import com.yupi.roj.common.ResultUtils;
import com.yupi.roj.constant.UserConstant;
import com.yupi.roj.exception.BusinessException;
import com.yupi.roj.model.dto.question.QuestionQueryRequest;
import com.yupi.roj.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.yupi.roj.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.yupi.roj.model.entity.Question;
import com.yupi.roj.model.entity.QuestionSubmit;
import com.yupi.roj.model.entity.User;
import com.yupi.roj.model.vo.QuestionSubmitVO;
import com.yupi.roj.service.QuestionSubmitService;
import com.yupi.roj.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/question_submit")
@Slf4j
@Deprecated //这个注解表示这个类过时了,启动的时候不会使用这个类
public class QuestionSubmitController {

    @Resource
    private QuestionSubmitService questionSubmitService;

    @Resource
    private UserService userService;

    /**
     * 提交题目
     *
     * @param questionSubmitAddRequest
     * @param request
     * @return resultNum
     */
    @PostMapping("/")
    public BaseResponse<Long> doQuestionSubmit(@RequestBody QuestionSubmitAddRequest questionSubmitAddRequest,
            HttpServletRequest request) {
        if (questionSubmitAddRequest == null || questionSubmitAddRequest.getQuestionId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        final User loginUser = userService.getLoginUser(request);
//        long questionId = questionSubmitAddRequest.getQuestionId();
        long questionSubmitId = questionSubmitService.doQuestionSubmit(questionSubmitAddRequest, loginUser);
        return ResultUtils.success(questionSubmitId);
    }

    /**
     * 分页获取题目提交列表（仅管理员）
     * 除管理员外,其他用户只能看到非答案等公开信息
     * @param questionSubmitQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<QuestionSubmitVO>> listQuestionSubmitByPage(@RequestBody QuestionSubmitQueryRequest questionSubmitQueryRequest, HttpServletRequest request) {
        long current = questionSubmitQueryRequest.getCurrent();
        long size = questionSubmitQueryRequest.getPageSize();
        //从数据库先查出要查的题目分页列表
        Page<QuestionSubmit> questionSubmitPage = questionSubmitService.page(new Page<>(current, size),
                questionSubmitService.getQueryWrapper(questionSubmitQueryRequest));
        //将题目分页列表进行脱敏并返回
        final User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(questionSubmitService.getQuestionSubmitVOPage(questionSubmitPage, loginUser));
    }
}


