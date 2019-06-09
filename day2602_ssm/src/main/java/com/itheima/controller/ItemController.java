package com.itheima.controller;

import com.itheima.pojo.Item;
import com.itheima.pojo.QueryVo;
import com.itheima.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.List;

@Controller
public class ItemController {
    @Autowired
    private ItemService service;
    @RequestMapping("itemList.action")
    public ModelAndView findAll(){
        ModelAndView modelAndView=new ModelAndView();
        List<Item> list = service.findAll();
        modelAndView.addObject("itemList",list);
        modelAndView.setViewName("itemList");
        return modelAndView;
    }

    @RequestMapping("itemEdit.action")
//    获取参数的第一种方式:通过内置的request,response,session对象获取
    public ModelAndView editItem(HttpServletRequest request){
        ModelAndView modelAndView =new ModelAndView();
        //获取参数信息
        String id = request.getParameter("id");
        int i = Integer.parseInt(id);
        Item item=service.editItemById(i);
        modelAndView.addObject("item",item);
        modelAndView.setViewName("itemEdit");
        return modelAndView;
    }

    //获取参数的第二种方式:直接在形参上填写参数的名
//    规则是参数名需要与提交的参数名相同
    /*
    解决乱码问题的方式:1.添加过滤器解决post方式乱码问题
    2.get方式乱码1)先编码再解码2)
     */
    /*  @RequestMapping("updateItem.action")
         public ModelAndView updateItem(int id,String name,float price,String detail) throws UnsupportedEncodingException {
        ModelAndView modelAndView=new ModelAndView();
        String sname = new String(name.getBytes("ISO-8859-1"), "utf-8");
        String sdetail = new String(detail.getBytes("ISO-8859-1"), "utf-8");
        Item item=new Item();
        item.setId(id);
        item.setName(sname);
        item.setPrice(price);
        item.setDetail(sdetail);
        int i=service.updateItem(item);
        if(i>0){
            modelAndView.setViewName("success");
        }else{
            modelAndView.setViewName("fail");
        }
        return modelAndView;
    }*/
    //提交的第二种方式
    @RequestMapping("updateItem.action")
    public String updateItem(Item item, Model model){
        int i=service.updateItem(item);
        if(i>0){
            return "success";
        }else{
            return "fail";
        }
    }

    @RequestMapping("queryItem.action")
    //@RequestParam(value = "item.name",required = false ,defaultValue = "0")
    public String queryByPriceAndName( QueryVo queryVo, Model model) throws UnsupportedEncodingException {
        System.out.println(queryVo);
        String name = queryVo.getItem().getName();
        String s = new String(name.getBytes("ISO8859-1"), "utf-8");
        System.out.println(name);
        System.out.println(s);
        queryVo.getItem().setName(s);
        System.out.println(queryVo.getItem().getPrice());
        List<Item> list= service.queryItem(queryVo);
        model.addAttribute("itemList",list );

        return "itemList";
    }

}
