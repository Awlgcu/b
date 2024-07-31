package com.yupi.rojcodesandbox.controller;

import com.yupi.rojcodesandbox.CodeSandbox;
import com.yupi.rojcodesandbox.JavaNativeCodeSandbox;
import com.yupi.rojcodesandbox.model.ExecuteCodeRequest;
import com.yupi.rojcodesandbox.model.ExecuteCodeResponse;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("/")
public class MainController {

    private String AUTH_REQUEST_HEADER = "auth";
    private String AUTH_REQUEST_SECRET = "secretKey";
    @Resource
    private JavaNativeCodeSandbox javaNativeCodeSandbox;

    @GetMapping("/health")
    public String healthCheck(){
        return "ok";
    }

    @PostMapping("/executeCode")
    ExecuteCodeResponse ExecuteCode(@RequestBody ExecuteCodeRequest executeCodeRequest, HttpServletRequest request, HttpServletResponse response){
        //先检验基本的权限,即请求头是否含有约定的密钥,没有则不能调用这个接口
        String auth = request.getHeader(AUTH_REQUEST_HEADER);
        if(!AUTH_REQUEST_SECRET.equals(auth)){
            response.setStatus(403);
            return null;
        }
        //调用这个接口实现具体业务
        if(executeCodeRequest == null){
            throw new RuntimeException("");
        }
        ExecuteCodeResponse executeCodeResponse = javaNativeCodeSandbox.ExecuteCode(executeCodeRequest);
        System.out.println("排错检查:"+executeCodeResponse);
        return executeCodeResponse;
    }
}
