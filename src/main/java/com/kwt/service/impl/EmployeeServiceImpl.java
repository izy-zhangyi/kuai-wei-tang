package com.kwt.service.impl;

import com.kwt.common.ReggieConstants;
import com.kwt.domain.Employee;
import com.kwt.mapper.EmployeeMapper;
import com.kwt.service.EmployeeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;


/*
 * ServiceImpl类是我们进行SQL操作中非常重要的一个类，
 * 通过MybatisPlus生成的各个实体类的XXXImpl都会继承ServiceImpl类那里继承全部的方法
 * IService 实现类（ 泛型：M 是 mapper 对象，T 是实体类 ）
 *
 * EmployeeServiceImpl 要实现 EmployeeService 接口，
 * EmployeeService接口 继承了IService 实现类（ 泛型：T 是实体类 ）
 * 简言之，EmployeeServiceImpl 要想实现 EmployeeService 接口，也要
 * 先继承 IService 实现类（ 泛型：M 是 mapper 对象，T 是实体类 ）再实现 EmployeeService接口
 */
@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {

    @Override
    public void saveEmployee(Employee employee) {
        //补充空白字段
        employee.setPassword(DigestUtils.md5DigestAsHex(ReggieConstants.PASSWORD_DEFAULT.getBytes()));
/*
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());
*/
        //id要不要处理
        //前端出bug了，id给你传过来了
        //id=1
        //我们在新增的时候，习惯上都会处理一下id
        employee.setId(null);
        this.save(employee);
    }
}
