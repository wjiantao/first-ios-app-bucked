package com.shiguang.context;

/**
 * 当前登录用户上下文。
 *
 * 使用 ThreadLocal 保存每个请求线程对应的用户 ID：
 * 拦截器解析完 token 后写入，Controller / Service 直接读取，
 * 无需在每个方法参数里传递 userId，减少接口签名噪音。
 * 注意：请求结束时必须在 afterCompletion 中 remove，防止线程池复用导致串号。
 */
public class UserContext {

    private static final ThreadLocal<String> CURRENT_ID = new ThreadLocal<>();

    public static void setCurrentId(String userId) {
        CURRENT_ID.set(userId);
    }

    public static String getCurrentId() {
        return CURRENT_ID.get();
    }

    public static void remove() {
        CURRENT_ID.remove();
    }

    private UserContext() {
    }
}
