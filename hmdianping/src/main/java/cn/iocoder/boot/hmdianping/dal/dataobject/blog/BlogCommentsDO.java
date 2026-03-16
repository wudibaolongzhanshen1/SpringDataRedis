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
 * 探店笔记评论表
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
@TableName(value = "tb_blog_comments", autoResultMap = true)
public class BlogCommentsDO extends BaseDO {


    /**
     * 主键
     */
    @com.baomidou.mybatisplus.annotation.TableId(value = "id", type = com.baomidou.mybatisplus.annotation.IdType.AUTO)
    @NotNull(message = "主键不能为空")
    private Long id;

    /**
     * 用户id
     */
    @TableField("user_id")
    @NotNull(message = "用户id不能为空")
    private Long userId;

    /**
     * 探店id
     */
    @TableField("blog_id")
    @NotNull(message = "探店id不能为空")
    private Long blogId;

    /**
     * 关联的1级评论id，如果是一级评论，则值为0
     */
    @TableField("parent_id")
    @NotNull(message = "关联的1级评论id，如果是一级评论，则值为0不能为空")
    private Long parentId;

    /**
     * 回复的评论id
     */
    @TableField("answer_id")
    private Long answerId;

    /**
     * 回复的内容
     */
    @TableField("content")
    @NotBlank(message = "回复的内容不能为空")
    @Size(max = 512, message = "回复的内容长度超限")
    private String content;

    /**
     * 点赞数
     */
    @TableField("liked")
    private Integer liked;

    /**
     * 状态，0：正常，1：被举报，2：禁止查看
     */
    @TableField("status")
    private Boolean status;

}