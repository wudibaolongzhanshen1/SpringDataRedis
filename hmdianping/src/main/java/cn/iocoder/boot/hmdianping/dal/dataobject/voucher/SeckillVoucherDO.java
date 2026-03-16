package cn.iocoder.boot.hmdianping.dal.dataobject.voucher;

    import cn.iocoder.boot.hmdianping.dal.dataobject.BaseDO;
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
    * 秒杀优惠券表，与优惠券表是一对一关系
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
@TableName(value = "tb_seckill_voucher", autoResultMap = true)
public class SeckillVoucherDO extends BaseDO {


        /**
        * 关联的优惠券id
        */
    @com.baomidou.mybatisplus.annotation.TableId(value = "voucher_id", type = com.baomidou.mybatisplus.annotation.IdType.AUTO)
    @NotNull(message = "关联的优惠券id不能为空")
    private Long voucherId;

        /**
        * 库存
        */
    @TableField("stock")
    @NotNull(message = "库存不能为空")
    private Integer stock;

        /**
        * 生效时间
        */
    @TableField("begin_time")
    @NotNull(message = "生效时间不能为空")
    private LocalDateTime beginTime;

        /**
        * 失效时间
        */
    @TableField("end_time")
    @NotNull(message = "失效时间不能为空")
    private LocalDateTime endTime;

}