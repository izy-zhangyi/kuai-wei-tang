package cn.itcast.reggie.controller;


import cn.itcast.reggie.common.R;
import cn.itcast.reggie.common.ReggieConstants;
import cn.itcast.reggie.domain.Employee;
import cn.itcast.reggie.service.EmployeeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@RestController //此注解包含了，@Component 与 @ResponseBody 的功能
@RequestMapping("/employee")
@Slf4j
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登录
     *
     * @param employee
     * @param request
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(@RequestBody Employee employee, HttpServletRequest request) {

        /*
            将前端返回的数据与数据库进行匹配验证
        */

        //isBlank：如果是null或者“”或者空格或者制表符则返回true。isBlank判空更加准确。
        //1、
        /**
         * 将页面提交的密码password进行非空与md5加密处理
         */
        if (employee == null || StringUtils.isBlank(employee.getUsername()) || StringUtils.isBlank(employee.getPassword())) {
            return R.error("参数为空");
        }
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //2、根据页面提交的用户名username查询数据库
        LambdaQueryWrapper<Employee> lqw = new LambdaQueryWrapper<Employee>();
        //eq(前台返回的值,数据库中所对应的表中的值);
        lqw.eq(Employee::getUsername, employee.getUsername());

        // 根据页面提交的 用户名 username 查询数据库中员工数据信息
        //用业务层调用getOne()，获取一条结果值(是否匹配到了)
        Employee emp = employeeService.getOne(lqw);

        //判断，前台返回的username and password 是否与数据库中的一样
        //1、结果值不为空，数据库中有与之匹配的相应的数据
        if (emp == null) {
            return R.error("登录失败，用户名或密码错误！");
        }
        if (!emp.getPassword().equals(password)) {
            return R.error("登录失败，用户名或密码错误！");
        }
        if (emp.getStatus() == 0) {
            return R.error("该员工账号已被禁用或注销！");
        }
        //以上判断若是全部通过，则登录成功
        //登录成功，将员工id存入Session, 并返回登录成功结果
        request.getSession().setAttribute("employee", emp.getId());
        log.info("登录成功！");
        return R.success(emp);
    }

    /**
     * 退出登录
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        request.getSession().removeAttribute("employee");
        return R.success("登出成功！");
    }

    /**
     * 添加数据
     * 注册员工信息
     *
     * @param employee
     * @param request
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Employee employee, HttpServletRequest request) {
        /**
         * 对初始密码加密处理
         */
        employee.setPassword(DigestUtils.md5DigestAsHex(ReggieConstants.PASSWORD_DEFAULT.getBytes()));

        /**
         * 获取员工账户创建及修改时间
         */
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        /**
         * 当一系列操作完之后，数据会保存到session之中
         * 获取新增的账户id
         * 由于id是由雪花算法生成，要用long类型接收，
         * 注：雪花算法最大可以生成的数字在不超过16位，
         * 一般id要转成字符
         */
        Long empId = (Long) request.getSession().getAttribute(ReggieConstants.EMPLOYEE_SESSION_KEY);

        /**
         * 获取账户创建人与修改人的信息
         */
        employee.setCreateUser(empId);
        employee.setUpdateUser(empId);
        /**
         * 最后，调用业务层接口中的方法，添加数据
         */
        this.employeeService.saveEmployee(employee);
        return R.success("新增员工成功");
    }

    /**
     * 查询数据
     * 分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    //<Page<Employee>>
    public R<Page<Employee>> pageEmployeeFind(Integer page, Integer pageSize, String name) {
        log.info("page:{},pageSize:{},name:{}", page, pageSize, name);

        /**
         * 分页查询 ===》 Page
         */

        Page<Employee> pageInfo = new Page<>(page, pageSize);

        LambdaQueryWrapper<Employee> lqw = new LambdaQueryWrapper<>();
        /**
         * 对 name 模糊查询
         * 前提条件是 name不能为空，之后再封装的类中的name值进行模糊匹配
         * ，最后将要被模糊匹配的值传给实体类
         */
        lqw.like(StringUtils.isNotBlank(name), Employee::getName, name);

        //根据账户创建时间降序排序
        lqw.orderByDesc(Employee::getCreateTime);

        /**
         * 调用业务层接口中的分页查询方法，分页查询
         * 先分页，再排序
         */

        employeeService.page(pageInfo, lqw);
        return R.success(pageInfo);
    }

    /**
     * 修改数据
     *
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> updateEmployee(@RequestBody Employee employee) {
        //一：先行判断：管理员不可被禁用
        if (employee.getId() == 1L && employee.getStatus() != null && employee.getStatus() == 0) {
            log.info("管理员不可被禁用");
            return R.error("管理员不可被禁用");
        }
        //二：对员工信息修改

        log.info(employee.toString());//日志打印，将日志以字符串形式打印

        // 1、 通过键获取所对应的id值
/*
        Long empId = (Long) request.getSession().getAttribute(ReggieConstants.EMPLOYEE_SESSION_KEY);
*/
        /**
         * 获取修改员工信息的时间
         * 获取被修改员工账户
         */
        //employee.setUpdateTime(LocalDateTime.now());
        /* employee.setUpdateUser(empId);*/
        this.employeeService.updateById(employee);
        return R.success("修改成功");
    }

    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id) {
        Employee employee = employeeService.getById(id);
        return R.success(employee);
    }

}