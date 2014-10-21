package org.fbi.trc.tqc.helper;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.Reader;

/**
 * User: zhanrui
 * Date: 2010-11-1
 * Time: 11:05:24
 */
public enum MybatisFactory {
    ORACLE;
    private SqlSessionFactory sessionFactory;

    MybatisFactory(){
        Reader reader = null;
        try {
            reader = Resources.getResourceAsReader("mybatis-config.xml");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("MYBATIS 参数文件读取错误!",e);
        }
        sessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }
    public SqlSessionFactory getInstance(){
        return  sessionFactory;
    }
}
