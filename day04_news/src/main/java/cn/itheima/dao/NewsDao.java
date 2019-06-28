package cn.itheima.dao;


import cn.itheima.pojo.News;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.springframework.jdbc.core.JdbcTemplate;

import java.beans.PropertyVetoException;

/**
 * @ClassName NewsDao
 * @Description TODO
 */
public class NewsDao extends JdbcTemplate {
    private static ComboPooledDataSource datasource;

    static {
        try {
            datasource = new ComboPooledDataSource();
            datasource.setDriverClass("com.mysql.jdbc.Driver");
            datasource.setJdbcUrl("jdbc:mysql://192.168.72.101:3306/gossip?characterEncoding=UTF-8");
            datasource.setUser("root");
            datasource.setPassword("root");
        } catch (PropertyVetoException e) {
            e.printStackTrace();
        }
    }

    public NewsDao(){
        //当该类创建的时候就会执行，本质上会创建一个JdbcTemplate
        super(datasource);
    }

    public void saveNews(News news){
        String sql = "insert into news (id,title,source,content,docurl,time,editor) values (?,?,?,?,?,?,?)";
        update(sql,news.getId(),news.getTitle(),news.getSource(),news.getContent(),news.getDocurl(),news.getTime(),news.getEditor());
    }
}