package com.duanzanjiu.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
@MapperScan("com.duanzanjiu.mapper")
public class MybatisPlusConfig {

    /**
     * 添加分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL)); // 如果配置多个插件, 切记分页最后添加
        // 如果有多数据源可以不配具体类型, 否则都建议配上具体的 DbType
        return interceptor;
    }
    //自动填充处理
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler(){
            @Override
            public void insertFill(MetaObject metaObject) {
             this.strictInsertFill(metaObject,"deleted",Integer.class,0);
             this.strictInsertFill(metaObject,"createTime", LocalDateTime.class,LocalDateTime.now());
             this.strictInsertFill(metaObject,"updateTime",LocalDateTime.class,LocalDateTime.now());
            }
            @Override
            public void updateFill(MetaObject metaObject) {
                this.strictInsertFill(metaObject,"updateTime",LocalDateTime.class,LocalDateTime.now());
            }
        };
    }

}