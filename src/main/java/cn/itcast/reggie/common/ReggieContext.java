package cn.itcast.reggie.common;

/**
 * Context上下文
 */
public class ReggieContext {
    /**
     *  基于ThreadLocal封装工具类，用户保存和获取当前登录用户id
     */
    private static final ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    /**
     * 将当前用户的id储存到ThreadLocal容器中
     */
    public static void set(Long id) {
        threadLocal.set(id);
    }

    /**
     * 获取存储的id
     * @return
     */
    public static Long get(){
      return   threadLocal.get();
    }

    /**
     * ThreadLocal用完之后，一定要跟一个remove手动清掉
     */
    public static void remove(){
        threadLocal.remove();
    }
}
