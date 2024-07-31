package com.yupi.roj.model.vo;

import cn.hutool.json.JSONUtil;
import com.yupi.roj.model.entity.Question;
import com.yupi.roj.model.dto.question.JudgeConfig;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import javax.json.Json;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 题目封装类
 * @TableName question
 */
@Data
public class QuestionVO implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 标签列表（json 数组）
     */
    private List<String> tags;

    /**
     * 题目提交数
     */
    private Integer submitNum;

    /**
     * 题目通过数
     */
    private Integer acceptedNum;

    /**
     * 判题配置（json 对象）
     */
    private com.yupi.roj.model.dto.question.JudgeConfig judgeConfig;

    /**
     * 点赞数
     */
    private Integer thumbNum;

    /**
     * 收藏数
     */
    private Integer favourNum;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建题目人的信息
     * */
    private UserVO userVO;

    private static final long serialVersionUID = 1L;
    /**
     * 包装类转对象
     *
     * @param questionVO
     * @return
     */
    public static Question voToObj(QuestionVO questionVO) {
        if (questionVO == null) {
            return null;
        }
        Question question = new Question();
        BeanUtils.copyProperties(questionVO, question);
        List<String> tagList = questionVO.getTags();
        if(tagList!=null){
            question.setTags(JSONUtil.toJsonStr(tagList));
        }
        JudgeConfig vojudgeConfig = questionVO.getJudgeConfig();
        if(vojudgeConfig!=null){
            question.setJudgeConfig(JSONUtil.toJsonStr(vojudgeConfig));
        }
        return question;
    }

    /**
     * 对象转包装类
     *
     * @param question
     * @return
     */
    public static QuestionVO objToVo(Question question) {
        if (question == null) {
            return null;
        }
        QuestionVO questionVO = new QuestionVO();
        BeanUtils.copyProperties(question, questionVO);
        //把标签从str转为对象
        questionVO.setTags(JSONUtil.toList(question.getTags(), String.class));
        //把判题配置从str转为对象
        questionVO.setJudgeConfig(JSONUtil.toBean(question.getJudgeConfig(), JudgeConfig.class));
        return questionVO;
    }
}