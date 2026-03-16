package cn.iocoder.boot.hmdianping.controller.voucher.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class SeckillVoucherVO {
    @NotNull(message = "关联的优惠券id不能为空")
    private Long voucherId;

    /**
     * 库存
     */
    @NotNull(message = "库存不能为空")
    private Integer stock;

    /**
     * 生效时间
     */
    @NotNull(message = "生效时间不能为空")
    private LocalDateTime beginTime;

    /**
     * 失效时间
     */
    @NotNull(message = "失效时间不能为空")
    private LocalDateTime endTime;


    @NotNull(message = "商铺id不能为空")
    private Long shopId;

    /**
     * 代金券标题
     */
    @NotBlank(message = "代金券标题不能为空")
    @Size(max = 255, message = "代金券标题长度超限")
    private String title;

    /**
     * 副标题
     */
    @Size(max = 255, message = "副标题长度超限")
    private String subTitle;

    /**
     * 使用规则
     */
    @Size(max = 1024, message = "使用规则长度超限")
    private String rules;

    /**
     * 支付金额，单位：分
     */
    @NotNull(message = "支付金额，单位：分不能为空")
    private Long payValue;

    /**
     * 抵扣金额，单位：分
     */
    @NotNull(message = "抵扣金额，单位：分不能为空")
    private Long actualValue;

    /**
     * 优惠券类型 (0:普通券, 1:秒杀券)
     */
    @NotNull(message = "优惠券类型 (0:普通券, 1:秒杀券)不能为空")
    private Boolean type;

    /**
     * 状态 (1:上架, 2:下架, 3:过期)
     */
    @NotNull(message = "状态 (1:上架, 2:下架, 3:过期)不能为空")
    private Boolean status;
}
