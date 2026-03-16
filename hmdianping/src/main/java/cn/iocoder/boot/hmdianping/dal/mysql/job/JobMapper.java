package cn.iocoder.boot.hmdianping.dal.mysql.job;

import cn.hutool.db.PageResult;
import cn.iocoder.boot.hmdianping.dal.dataobject.job.JobDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 定时任务 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface JobMapper extends BaseMapper<JobDO> {

    default JobDO selectByHandlerName(String handlerName) {
        return selectOne(new LambdaQueryWrapper<JobDO>()
                .eq(JobDO::getHandlerName, handlerName)); // eq 表示 "等于"
    }


}
