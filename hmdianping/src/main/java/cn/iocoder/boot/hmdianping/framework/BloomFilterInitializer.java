package cn.iocoder.boot.hmdianping.framework;


import cn.iocoder.boot.hmdianping.dal.dataobject.blog.BlogDO;
import cn.iocoder.boot.hmdianping.dal.dataobject.follow.FollowDO;
import cn.iocoder.boot.hmdianping.dal.dataobject.shop.ShopDO;
import cn.iocoder.boot.hmdianping.dal.mysql.blog.BlogMapper;
import cn.iocoder.boot.hmdianping.dal.mysql.follow.FollowMapper;
import cn.iocoder.boot.hmdianping.dal.mysql.shop.ShopMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.redisson.api.RBloomFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BloomFilterInitializer implements CommandLineRunner {
    @Autowired
    private ShopMapper shopMapper;
    @Autowired
    private FollowMapper followMapper;
    @Autowired
    private BlogMapper blogMapper;
    @Autowired
    private RBloomFilter<Long> shopBloomFilter;
    @Autowired
    private RBloomFilter<Long> followBloomFilter;
    @Autowired
    private RBloomFilter<Long> blogBloomFilter;

    @Override
    public void run(String... args) {
        // 实际工程中，如果数据量极大，建议分批读取
        List<ShopDO> shopList = shopMapper.selectList(new QueryWrapper<ShopDO>().select("id"));
        List<FollowDO> followList = followMapper.selectList(new QueryWrapper<FollowDO>().select("id"));
        List<BlogDO> blogList = blogMapper.selectList(new QueryWrapper<BlogDO>().select("id"));
        shopList.forEach(shop -> shopBloomFilter.add(shop.getId()));
        followList.forEach(follow -> followBloomFilter.add(follow.getUserId()));
        blogList.forEach(blog -> blogBloomFilter.add(blog.getId()));
    }
}
