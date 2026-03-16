package cn.iocoder.boot.hmdianping.dal.dataobject.follow;

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
 * 用户关注表
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
@TableName(value = "tb_follow", autoResultMap = true)
public class FollowDO extends BaseDO {


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
     * 被关注的用户id
     */
    @TableField("follow_user_id")
    @NotNull(message = "被关注的用户id不能为空")
    private Long followUserId;

}