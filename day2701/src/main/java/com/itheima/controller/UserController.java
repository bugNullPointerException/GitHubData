package com.itheima.controller;

import com.itheima.pojo.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;

@Controller
public class UserController {

    @RequestMapping("toLogin.action")
    public String toLogin(){
        return "forward:/WEB-INF/jsp/login.jsp";
    }

    @RequestMapping("login.action")
    public String login(String username ,String password,HttpSession session){
        if("zhangsan".equalsIgnoreCase(username.trim()) && "123".equals(password.trim())){
            //把数据放到session中
            User user = new User();
            user.setUsername("zhangsan");
            user.setPassword("123");
            session.setAttribute("user",username);
            //页面跳转
            return "redirect:/itemList.action";
        }
//        失败
        session.setAttribute("msg","用户名或密码错误");
        return "redirect:/tologin.action";//重定向到登陆页面
    }

}
