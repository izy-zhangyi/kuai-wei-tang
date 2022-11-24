package cn.itcast.reggie.controller;

import cn.itcast.reggie.common.R;
import cn.itcast.reggie.domain.User;
import cn.itcast.reggie.service.UserService;
import cn.itcast.reggie.utils.ValidateCodeUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController //此注释，包含了 @Component 与 @ResponseBody 这两个注解的方法
@Slf4j
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 发送手机验证码
     * @param user
     * @param session
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session) {
        //获取手机号
        String phone = user.getPhone();

        //只是随机生成验证码
        if (StringUtils.isNotBlank(phone)) {
            //随机生成验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code:{}",code);

            //调用阿里云提供的短信服务API 完场发送短信
            //SMSUtils.sendMessage("瑞吉外卖",phone,code)

            //需要将生成的验证码保存到session中
            session.setAttribute(phone, code);
            return R.success("手机验证码短信发送成功");
        }
        return R.error("短信发送失败");
    }

    /**
     * 移动端客户登录
     * @param map
     * @param session
     * @return
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map,HttpSession session){
        log.info(map.toString());

        //获取手机号
        String phone = map.get("phone").toString();
        //获取验证码
        String code = map.get("code").toString();

        //在从session中获取已经保存过的验证码，
        Object codeInSession = session.getAttribute(phone);

        //验证码比对，将页面提交的验证码与session中保存的验证码比对
        if (codeInSession != null&& codeInSession.equals(code)) {
            //比对成功，说明就是登录成功了

            //登录成功之后，就要查出该手机号所对应的数据
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            //eq()比对，获取数据，
            queryWrapper.eq(User::getPhone,phone);

            //获取当前手机号所对应的一条数据
            User user = userService.getOne(queryWrapper);
            if (user == null) { //user为空---》新用户，（查不到这个user的信息，新用户）
                //判断当前手机号对应的用户是否为新用户，如果是新用户就会自动完成注册
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            //新用户注册自动注册成功，将数据保存到session中
            session.setAttribute("user",user.getId());
            return R.success(user);
        }
        return R.error("登录失败");
    }

}
