package cn.itcast.reggie.controller;

import cn.itcast.reggie.common.R;
import cn.itcast.reggie.common.ReggieConstants;
import cn.itcast.reggie.domain.User;
import cn.itcast.reggie.exception.BusinessException;
import cn.itcast.reggie.req.UserLoginReq;
import cn.itcast.reggie.service.UserService;
import cn.itcast.reggie.utils.ValidateCodeUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.concurrent.TimeUnit;

@RestController //此注释，包含了 @Component 与 @ResponseBody 这两个注解的方法
@Slf4j
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 发送手机验证码
     *
     * @param user
     * @param session
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session) {
        if (user == null && StringUtils.isNotBlank(user.getPhone())) {
            throw new BusinessException("手机号为空");
        }
        //获取手机号
        String phone = user.getPhone();

        //随机生成验证码
        String code = ValidateCodeUtils.generateValidateCode(4).toString();
        log.info("code:{}", code);

        //调用阿里云提供的短信服务API 完场发送短信
        //SMSUtils.sendMessage("瑞吉外卖",phone,code)

/*            //需要将生成的验证码保存到session中
                redis 修改后就不需要将将验证码存到session中了
            session.setAttribute(phone, code);
*/
        //将随机生成的验证码缓存到Redis中去
        redisTemplate.opsForValue().set(ReggieConstants.USER_MSG_CODE+phone, code, 5, TimeUnit.MINUTES);
        return R.success("手机验证码短信发送成功");
    }

    /**
     * 移动端客户登录
     *
     * @param userRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody UserLoginReq userRequest, HttpServletRequest request) {
        if (userRequest == null&&StringUtils.isNotBlank(userRequest.getCode())) {
            throw new BusinessException("参数为空");
        }
        log.info("request:{}",userRequest);
        //通过用户手机号在Redis中拿到对应的验证码
        //拿到请求登录的用户手机号
        String phoneKey = ReggieConstants.USER_MSG_CODE + userRequest.getPhone();
        //拿到发送的登录验证码
        String phoneCode = (String) redisTemplate.opsForValue().get(phoneKey);
        if (!userRequest.getCode().equals(phoneCode)) {
            return R.error("验证码错误或已经失效");
        }
        //如果用户输入的手机号与请求登录的手机号不匹配,
        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getPhone, userRequest.getPhone()), false);
        if (user == null) { //user为空---》新用户，（查不到这个user的信息，新用户）
            //判断当前手机号对应的用户是否为新用户，如果是新用户就会自动完成注册
            user = new User();
            user.setPhone(phoneKey);
            user.setStatus(1);
            userService.save(user);
        }
        if (user.getStatus().equals("0")) {
            return R.error("用户已被禁用");
        }
        //新用户注册自动注册成功，将数据保存到session中
        request.getSession().setAttribute("user",user.getId());
        //用户登录成功，则直接在Redis的缓存中删除已经登录过的验证码
        redisTemplate.delete(phoneKey);
        return R.success(user);

    }

    @PostMapping("/loginout")
    public R<String> loginout(HttpServletRequest request) {
        request.getSession().removeAttribute("employee");
        return R.success("退出登录成功！");
    }

}