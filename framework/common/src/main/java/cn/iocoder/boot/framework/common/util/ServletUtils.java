package cn.iocoder.boot.framework.common.util;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class ServletUtils {

    /**
     * 返回 JSON 字符串
     *
     * @param response 响应对象
     * @param object   对象（CommonResult 等）
     */
    public static void writeJSON(HttpServletResponse response, Object object) {
        try {
            // 1. 设置响应头
            response.setContentType("application/json;charset=UTF-8");
            // 2. 将对象转为 JSON 字符串 (建议使用 Jackson)
            String json = JsonUtils.toJsonString(object);
            // 3. 写入响应
            response.getWriter().write(json);
        } catch (IOException e) {
            // 在框架层通常只打印日志或抛出运行时异常
            throw new RuntimeException(e);
        }
    }
}
