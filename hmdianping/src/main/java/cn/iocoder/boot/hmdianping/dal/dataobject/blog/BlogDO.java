package cn.iocoder.boot.hmdianping.dal.dataobject.blog;

import cn.iocoder.boot.hmdianping.dal.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.*;
import lombok.experimental.Accessors;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import jakarta.validation.constraints.*;

/**
 * <p>
 * 探店笔记表
 * </p>
 *
 * @author czl
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName(value = "tb_blog", autoResultMap = true)
public class BlogDO extends BaseDO {


    /**
     * 主键
     */
    @com.baomidou.mybatisplus.annotation.TableId(value = "id", type = com.baomidou.mybatisplus.annotation.IdType.AUTO)
    @NotNull(message = "主键不能为空")
    private Long id;

    /**
     * 商户id
     */
    @TableField("shop_id")
    @NotNull(message = "商户id不能为空")
    private Long shopId;

    /**
     * 用户id
     */
    @TableField("user_id")
    @NotNull(message = "用户id不能为空")
    private Long userId;

    /**
     * 标题
     */
    @TableField("title")
    @NotBlank(message = "标题不能为空")
    @Size(max = 255, message = "标题长度超限")
    private String title;

    /**
     * 探店的照片，最多9张，多张以","隔开
     */
    @TableField("images")
    @Size(max = 1024, message = "探店的照片，最多9张，多张以\",\"隔开长度超限")
    private String images;

    /**
     * 探店的文字描述
     */
    @TableField("content")
    @NotBlank(message = "探店的文字描述不能为空")
    @Size(max = 65535, message = "探店的文字描述长度超限")
    private String content;

    /**
     * 点赞数量
     */
    @TableField("liked")
    private Integer liked;

    /**
     * 评论数量
     */
    @TableField("comments")
    private Integer comments;

}