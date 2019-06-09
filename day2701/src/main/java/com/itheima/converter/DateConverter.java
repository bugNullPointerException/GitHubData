package com.itheima.converter;

import org.springframework.core.convert.converter.Converter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateConverter implements Converter<String,Date> {
    //通过该方法来自定义接受数据的字符串类型
    public Date convert(String source) {
        try {
            //把字符串转换成日期类型
            SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = simpleDateFormat.parse(source);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
