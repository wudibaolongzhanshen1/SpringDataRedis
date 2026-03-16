package cn.iocoder.boot.hmdianping.controller.blog.vo;

import cn.iocoder.boot.hmdianping.dal.dataobject.blog.BlogDO;
import lombok.Data;

import java.util.List;


@Data
public class ScrollRespVO {
    private List<BlogDO> list; // 博客内容列表
    private Long minTime;      // 本次查询结果中的最小时间戳（即下次的 max）
    private Integer offset;    // 下次请求需要的偏移量
}
