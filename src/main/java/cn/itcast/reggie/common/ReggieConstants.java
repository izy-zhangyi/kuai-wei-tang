package cn.itcast.reggie.common;


public class ReggieConstants {

    /**
     * 员工存储在session中的键
     */
    public static final String EMPLOYEE_SESSION_KEY = "employee";

    /**
     * 与前端约定的，没有登录的返回信息
     */

    public static final String NOT_LOGIN_KEY = "NOTLOGIN";

    /**
     * 移动端在session中储存的键
     */

    public static final String USER_SESSION_KEY = "user";
    /**
     * 默认放行路径
     */
    public static final String[] URIS = new String[]{
            "/backend/**", "/front/**",
            "/employee/login", "/employee/logout",
            "/common/upload","/common/download",
            "/user/sendMsg","/user/login",
            "/doc.html",
            "/webjars/**",
            "/swagger-resources",
            "/v2/api-docs"

    };

    /**
     * 为用户设置默认密码
     */
    public static final String PASSWORD_DEFAULT = "123456";
    public static final Object USER_MSG_CODE = "userCode";
}
