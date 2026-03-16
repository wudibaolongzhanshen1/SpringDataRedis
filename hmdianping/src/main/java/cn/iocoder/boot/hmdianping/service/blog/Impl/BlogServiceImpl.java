package cn.iocoder.boot.hmdianping.service.blog.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.boot.framework.security.core.LoginUser;
import cn.iocoder.boot.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.boot.hmdianping.controller.blog.vo.ScrollRespVO;
import cn.iocoder.boot.hmdianping.dal.dataobject.blog.BlogDO;
import cn.iocoder.boot.hmdianping.dal.dataobject.follow.FollowDO;
import cn.iocoder.boot.hmdianping.dal.dataobject.user.UserDO;
import cn.iocoder.boot.hmdianping.dal.mysql.blog.BlogMapper;
import cn.iocoder.boot.hmdianping.enums.SystemConstants;
import cn.iocoder.boot.hmdianping.service.blog.BlogService;
import cn.iocoder.boot.hmdianping.service.follow.FollowService;
import cn.iocoder.boot.hmdianping.service.user.UserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.boot.hmdianping.enums.RedisKeyConstants.BLOG_LIKED_KEY;
import static cn.iocoder.boot.hmdianping.enums.RedisKeyConstants.FEED_KEY;

/**
 * <p>
 * 探店笔记表 服务实现类
 * </p>
 *
 * @author czl
 * @since 2026-03-11
 */
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, BlogDO> implements BlogService {


    @Autowired
    private UserService userService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private FollowService followService;

    @Override
    public List<BlogDO> queryHotBlog(Integer current) {
        // 根据用户查询
        Page<BlogDO> page = query()
                .orderByDesc("liked")
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页数据
        return page.getRecords();
    }


    @Override
    public BlogDO queryBlogById(Long id) {
        return getById(id);
    }

    @Override
    public void likeBlog(Long id) {
        // 1.获取登录用户
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        // 2.判断当前登录用户是否已经点赞
        String key = BLOG_LIKED_KEY + id;
        Double score = stringRedisTemplate.opsForZSet().score(key, String.valueOf(userId));
        if (score == null) {
            // 3.如果未点赞，可以点赞
            // 3.1.数据库点赞数 + 1
            boolean isSuccess = update().setSql("liked = liked + 1").eq("id", id).update();
            // 3.2.保存用户到Redis的set集合  zadd key value score
            if (isSuccess) {
                stringRedisTemplate.opsForZSet().add(key, userId.toString(), System.currentTimeMillis());
            }
        } else {
            // 4.如果已点赞，取消点赞
            // 4.1.数据库点赞数 -1
            boolean isSuccess = update().setSql("liked = liked - 1").eq("id", id).update();
            // 4.2.把用户从Redis的set集合移除
            if (isSuccess) {
                stringRedisTemplate.opsForZSet().remove(key, userId.toString());
            }
        }
    }

    @Override
    public List<UserDO> queryBlogLikes(Long id) {
        String key = BLOG_LIKED_KEY + id;
        // 1.查询top5的点赞用户 zrange key 0 4
        Set<String> top5 = stringRedisTemplate.opsForZSet().range(key, 0, 4);
        if (top5 == null || top5.isEmpty()) {
            return Collections.emptyList();
        }
        // 2.解析出其中的用户id
        List<Long> ids = top5.stream().map(Long::valueOf).collect(Collectors.toList());
        String idStr = StrUtil.join(",", ids);
        // 3.根据用户id查询用户 WHERE id IN ( 5 , 1 ) ORDER BY FIELD(id, 5, 1)
        List<UserDO> userDTOS = userService.query()
                .in("id", ids).last("ORDER BY FIELD(id," + idStr + ")").list()
                .stream()
                .map(user -> BeanUtil.copyProperties(user, UserDO.class))
                .collect(Collectors.toList());
        // 4.返回
        return userDTOS;
    }

    @Override
    @Transactional
    public Long saveBlog(BlogDO blog) throws Exception {
        // 1.获取登录用户
        LoginUser user = SecurityFrameworkUtils.getLoginUser();
        blog.setUserId(user.getId());
        // 2.保存探店笔记
        boolean isSuccess = save(blog);
        if (!isSuccess) {
            throw new Exception("新增笔记失败!");
        }
        return blog.getId();
    }

    @Override
    public ScrollRespVO queryBlogOfFollow(Long max, Integer offset) {
        // 1.获取当前用户
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        // 2.查询收件箱 ZREVRANGEBYSCORE key Max Min LIMIT offset count
        String key = FEED_KEY + userId;
        Set<ZSetOperations.TypedTuple<String>> typedTuples = stringRedisTemplate.opsForZSet()
                .reverseRangeByScoreWithScores(key, 0, max, offset, 2);
        // 3.非空判断
        if (typedTuples == null || typedTuples.isEmpty()) {
            return new ScrollRespVO();
        }
        // 4.解析数据：blogId、minTime（时间戳）、offset
        List<Long> ids = new ArrayList<>(typedTuples.size());
        long minTime = 0; // 2
        int os = 1; // 2
        for (ZSetOperations.TypedTuple<String> tuple : typedTuples) { // 5 4 4 2 2
            // 4.1.获取id
            ids.add(Long.valueOf(tuple.getValue()));
            // 4.2.获取分数(时间戳）
            long time = tuple.getScore().longValue();
            if (time == minTime) {
                os++;
            } else {
                minTime = time;
                os = 1;
            }
        }
        String idStr = StrUtil.join(",", ids);
        List<BlogDO> blogs = query().in("id", ids).last("ORDER BY FIELD(id," + idStr + ")").list();
        // 6.封装并返回
        ScrollRespVO r = new ScrollRespVO();
        r.setList(blogs);
        r.setOffset(os);
        r.setMinTime(minTime);
        return r;
    }
}