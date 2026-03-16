package cn.iocoder.boot.hmdianping.dal.dataobject.voucher;

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
    * 优惠券表
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
@TableName(value = "tb_voucher", autoResultMap = true)
public class VoucherDO extends BaseDO {


        /**
        * 主键
        */
    @com.baomidou.mybatisplus.annotation.TableId(value = "id", type = com.baomidou.mybatisplus.annotation.IdType.AUTO)
    @NotNull(message = "主键不能为空")
    private Long id;

        /**
        * 商铺id
        */
    @TableField("shop_id")
    private Long shopId;

        /**
        * 代金券标题
        */
    @TableField("title")
    @NotBlank(message = "代金券标题不能为空")
    @Size(max = 255, message = "代金券标题长度超限")
    private String title;

        /**
        * 副标题
        */
    @TableField("sub_title")
    @Size(max = 255, message = "副标题长度超限")
    private String subTitle;

        /**
        * 使用规则
        */
    @TableField("rules")
    @Size(max = 1024, message = "使用规则长度超限")
    private String rules;

        /**
        * 支付金额，单位：分
        */
    @TableField("pay_value")
    @NotNull(message = "支付金额，单位：分不能为空")
    private Long payValue;

        /**
        * 抵扣金额，单位：分
        */
    @TableField("actual_value")
    @NotNull(message = "抵扣金额，单位：分不能为空")
    private Long actualValue;

        /**
        * 优惠券类型 (0:普通券, 1:秒杀券)
        */
    @TableField("type")
    @NotNull(message = "优惠券类型 (0:普通券, 1:秒杀券)不能为空")
    private Boolean type;

        /**
        * 状态 (1:上架, 2:下架, 3:过期)
        */
    @TableField("status")
    @NotNull(message = "状态 (1:上架, 2:下架, 3:过期)不能为空")
    private Boolean status;

}