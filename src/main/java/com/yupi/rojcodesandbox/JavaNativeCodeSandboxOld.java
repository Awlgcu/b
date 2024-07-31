package com.yupi.rojcodesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.dfa.FoundWord;
import cn.hutool.dfa.WordTree;
import com.yupi.rojcodesandbox.model.ExecuteCodeRequest;
import com.yupi.rojcodesandbox.model.ExecuteCodeResponse;
import com.yupi.rojcodesandbox.model.ExecuteMessage;
import com.yupi.rojcodesandbox.model.JudgeInfo;
import com.yupi.rojcodesandbox.utils.ProcessUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class JavaNativeCodeSandboxOld implements CodeSandbox{
    private final String GLOBAL_CODE_DIR_NAME = "tmpCode";
    private final String GLOBAL_JAVA_CLASS_NAME = "Main.java";
    //定义守护线程的时间限制,防止死循环
    private final long TIME_OUT = 500;
    //定义禁用命令的黑名单,防止用户执行非法命令
    private static final List<String> blackList = Arrays.asList("Files", "exec");

    private static final WordTree WORDTREE;
    static{
        WORDTREE = new WordTree();
        WORDTREE.addWords(blackList);
    }

    public static void main(String[] args) {
        JavaNativeCodeSandboxOld javaNativeCodeSandboxOld = new JavaNativeCodeSandboxOld();
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setInputList(Arrays.asList("1 2", "3 4"));
        //利用hutool包下的resourceUtil类读取本地文件中的内容
//        第一种方式读取代码
        String code = ResourceUtil.readStr("testCode/simpleComputeArgs/Main.java", StandardCharsets.UTF_8);
//        String code = ResourceUtil.readStr("testCode/unsafeCode/Main.java", StandardCharsets.UTF_8);

        //第二种方式读取代码
//        String code = ResourceUtil.readStr("testCode/simpleCompute/Main.java", StandardCharsets.UTF_8);
        executeCodeRequest.setCode(code);
        executeCodeRequest.setLanguage("java");

        ExecuteCodeResponse executeCodeResponse = javaNativeCodeSandboxOld.ExecuteCode(executeCodeRequest);
        System.out.println(executeCodeResponse);
    }
    @Override
    public ExecuteCodeResponse ExecuteCode(ExecuteCodeRequest executeCodeRequest) {
        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();
        String language = executeCodeRequest.getLanguage();

        //0.校验代码,防止执行危险命令.利用的是hutool工具包下的wordtree类,这个类可以工具关键字建立字典树
        // ,然后检查一段文本中是否有这些关键字!
        FoundWord foundWord = WORDTREE.matchWord(code);
        if(foundWord!=null){
            System.out.println("禁止执行危险命令或代码:"+foundWord.getFoundWord());
            return null;
        }

        //1.把用户代码保存为文件
        //1.1拿到当前工作目录
        String userDir = System.getProperty("user.dir");
        String globalCodePathName = userDir+ File.separator+GLOBAL_CODE_DIR_NAME;
        //用separator不直接使用某种斜线,因为windows操作系统的路径分隔符跟linux不一样!

        if(!FileUtil.exist(globalCodePathName)){
            //如果文件夹不存在,则新建
            FileUtil.mkdir(globalCodePathName);
        }
        //1.2.利用uuid生成随机文件夹,因为我们的文件名都是Main.java,不能让这些文件处于一个文件夹
        String userCodeParentPath = globalCodePathName+ File.separator+ UUID.randomUUID();
        String userCodePath = userCodeParentPath+File.separator+GLOBAL_JAVA_CLASS_NAME;
        //1.3.将代码写进文件!
        File userCodeFile = FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);

        //2.编译代码,得到.class文件
        //2.1执行刚刚写的文件
        String compileCmd = String.format("javac -encoding utf-8 %s", userCodeFile.getAbsolutePath());
        try {
            Process compileProcess = Runtime.getRuntime().exec(compileCmd);
            //拿到执行编译代码的进程,使用封装的工具类去获取编译结果
            ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(compileProcess, "编译");
            System.out.println(executeMessage);
        } catch (Exception e) {
            //如果出现异常,则返回异常的executeCodeResponse对象
            return getErrorResponse(e);
        }

        //3.执行编译后的文件
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        for (String inputArgs : inputList) {
            String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s Main %s", userCodeParentPath, inputArgs);

            try {
                Process runProcess = Runtime.getRuntime().exec(runCmd);

                //开启新线程去监控上面执行代码的线程,如果那个线程的在5s后都没执行完,那说明出错了,可以直接关闭线程了
                new Thread(()->{
                    try {
                        Thread.sleep(TIME_OUT);
                        System.out.println("超时了");
                        runProcess.destroy();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }).start();

                //拿到执行运行代码的进程,使用封装的工具类去获取运行结果
                //第一种方式的接口
                ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(runProcess, "运行");
                //第二种方式的接口
//                ExecuteMessage executeMessage = ProcessUtils.runInteractProcessAndGetMessage(runProcess, "运行", inputArgs);
                executeMessageList.add(executeMessage);
                System.out.println(executeMessage);
            } catch (IOException e) {
                return getErrorResponse(e);
            }
        }

        //4.整理输出信息,将执行结果信息封装到executeCodeResponse里面
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        List<String> outputList = new ArrayList<>();
        long maxTime=0;
        for (ExecuteMessage executeMessage : executeMessageList) {
            String errorMessage = executeMessage.getErrorMessage();
            if(StrUtil.isNotBlank(errorMessage)){
                //如果执行出现错误了,返回设置状态填充错误信息并直接结束循环
                executeCodeResponse.setStatus(3);//3表示代码执行中出错了
                executeCodeResponse.setMessage(errorMessage);
                break;
            }
            Long time = executeMessage.getTime();
            //这里选择的是简便的方法,即只获取所有执行结果中最长的时间进行校验!
            if(time!=null){
                maxTime = Math.max(maxTime, time);
            }
            outputList.add(executeMessage.getMessage());
        }
        //判断每个样例是否都成功执行,设置状态码为1
        if(outputList.size()==executeMessageList.size()){
            executeCodeResponse.setStatus(1);
        }
        executeCodeResponse.setOutputList(outputList);
        JudgeInfo judgeInfo = new JudgeInfo();
        //TODO 待完成,如何查看程序执行内存
//        judgeInfo.setMemory(108);
        judgeInfo.setTime(maxTime);
        executeCodeResponse.setJudgeInfo(judgeInfo);

        //5.执行完一个样例后的文件夹及文件应该被清理
        if(userCodeFile.getParentFile()!=null){
            boolean del = FileUtil.del(userCodeParentPath);
            System.out.println("删除"+(del ? "成功":"失败"));
        }

        //6.错误处理提升程序健壮性

        return executeCodeResponse;
    }

    /**
     * 遇到异常返回异常反馈的方法
     * @param e
     * @return
     */
    public ExecuteCodeResponse getErrorResponse(Throwable e){
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setOutputList(Collections.emptyList());
        executeCodeResponse.setMessage(e.getMessage());
        executeCodeResponse.setStatus(2);//2表示代码还没执行就出错,内部错误或者编译错误
        executeCodeResponse.setJudgeInfo(null);
        return executeCodeResponse;
    }
}
