package cn.iocoder.boot.hmdianping.dal.dataobject.voucher;

    import cn.iocoder.boot.hmdianping.dal.dataobject.BaseDO;
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
    * 优惠券订单表
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
@TableName(value = "tb_voucher_order", autoResultMap = true)
public class VoucherOrderDO extends BaseDO {


        /**
        * 主键
        */
    @com.baomidou.mybatisplus.annotation.TableId(value = "id", type = com.baomidou.mybatisplus.annotation.IdType.AUTO)
    @NotNull(message = "主键不能为空")
    private Long id;

        /**
        * 下单的用户id
        */
    @TableField("user_id")
    @NotNull(message = "下单的用户id不能为空")
    private Long userId;

        /**
        * 购买的代金券id
        */
    @TableField("voucher_id")
    @NotNull(message = "购买的代金券id不能为空")
    private Long voucherId;

        /**
        * 支付方式 1：余额支付；2：支付宝；3：微信
        */
    @TableField("pay_type")
    private Boolean payType;

        /**
        * 订单状态，1：未支付；2：已支付；3：已核销；4：已取消；5：退款中；6：已退款
        */
    @TableField("status")
    private Boolean status;

        /**
        * 支付时间
        */
    @TableField("pay_time")
    private LocalDateTime payTime;

        /**
        * 核销时间
        */
    @TableField("use_time")
    private LocalDateTime useTime;

        /**
        * 退款时间
        */
    @TableField("refund_time")
    private LocalDateTime refundTime;

}