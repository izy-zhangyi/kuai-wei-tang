package cn.itcast.reggie.service;

import cn.itcast.reggie.domain.Employee;
import com.baomidou.mybatisplus.extension.service.IService;
/*
 * ServiceImpl类是我们进行SQL操作中非常重要的一个类，
 * 通过MybatisPlus生成的各个实体类的XXXImpl都会继承ServiceImpl类那里继承全部的方法
 * 直接执行业务操作, 简化业务层代码实现
 * 1、IService 实现类（ 泛型：M 是 mapper 对象，T 是实体类 ）
 * 2、IService 实现类（ 泛型：T 是实体类 ）
 */
public interface EmployeeService extends IService<Employee> {
    void saveEmployee(Employee employee);
}
