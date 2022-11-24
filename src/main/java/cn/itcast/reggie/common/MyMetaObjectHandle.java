package cn.itcast.reggie.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
/**
 * 元数据处理
 */
public class MyMetaObjectHandle implements MetaObjectHandler {
    /*//获取id
    Long id = ReggieContext.get();
    //在这获取id，会报出异常（无法添加菜品）
    */
    /**
     * 新增数据时自动填充
     * @param metaObject
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        Long id = ReggieContext.get();
        log.info("新增填充公共字段");
        /**
         * 添加数据时，
         * 1、获取创建时间，
         * 2、获取被创建用户id
         * 3、获取创建最后修改的时间
         * 4、获取创建账户的最后修改id
         * 一系列完成，创建成功
         */
        //强制填充
/*
        metaObject.setValue("createTime", LocalDateTime.now());
        metaObject.setValue("createUser", 1L);
        metaObject.setValue("updateTime", LocalDateTime.now());
        metaObject.setValue("updateUser", 1L);
*/

        /**
         * 默认不为空的话，就不会填充
         */
        this.strictInsertFill(metaObject,"createTime",LocalDateTime.class,LocalDateTime.now());
        this.strictInsertFill(metaObject,"updateTime",LocalDateTime.class,LocalDateTime.now());
        this.strictInsertFill(metaObject,"createUser",Long.class, id);
        this.strictInsertFill(metaObject,"updateUser",Long.class, id);

    }

    /**
     * 修改数据时自动填充
     * @param metaObject
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        Long id = ReggieContext.get();
        log.info("修改填充公共字段");
        /**
         * 修改所有数据的其中一条数据
         * 1、获取修改账户信息的时间
         * 2、获取修改账户信息的id
         * 一系列操作完成，修改成功
         */
        metaObject.setValue("updateTime",LocalDateTime.now());
        metaObject.setValue("updateUser",id);
        /**
         * 修改不可this调用
         */
/*      this.strictUpdateFill(metaObject,"updateTime",LocalDateTime.class,LocalDateTime.now());
        this.strictUpdateFill(metaObject,"updateUser",Long.class,id);
*/
    }
}
