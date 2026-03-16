package cn.iocoder.boot.hmdianping.canal.context;

import cn.iocoder.boot.hmdianping.canal.handler.EntryHandler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class CanalHandlerContext {

    // Key 为表名，Value 为对应的处理器
    private final Map<String, EntryHandler> handlerMap = new ConcurrentHashMap<>();

    // 利用 Spring 构造注入，自动获取所有 EntryHandler 的实现类
    public CanalHandlerContext(List<EntryHandler> handlers) {
        handlers.forEach(handler -> handlerMap.put(handler.getTableName(), handler));
    }

    /**
     * 获取所有已注册的表名，用逗号分隔，用于 Canal 订阅
     * 格式示例: "hmdp.tb_follow,hmdp.tb_user"
     */
    public String getFilterRegex(String schema) {
        if (handlerMap.isEmpty()) {
            return ".*\\..*"; // 如果没有处理器，默认监听所有（或者返回空）
        }
        // 将表名提取出来，加上库名前缀，并用逗号拼接
        return handlerMap.keySet().stream()
                .map(tableName -> schema + "." + tableName)
                .collect(Collectors.joining(","));
    }

    public EntryHandler getHandler(String tableName) {
        return handlerMap.get(tableName);
    }
}