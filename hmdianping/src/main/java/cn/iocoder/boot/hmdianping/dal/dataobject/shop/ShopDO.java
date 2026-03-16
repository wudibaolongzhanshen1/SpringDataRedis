package cn.iocoder.boot.hmdianping.dal.dataobject.shop;

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
    * 商铺表
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
@TableName(value = "tb_shop", autoResultMap = true)
public class ShopDO extends BaseDO {


        /**
        * 主键
        */
    @com.baomidou.mybatisplus.annotation.TableId(value = "id", type = com.baomidou.mybatisplus.annotation.IdType.AUTO)
    @NotNull(message = "主键不能为空")
    private Long id;

        /**
        * 商铺名称
        */
    @TableField("name")
    @NotBlank(message = "商铺名称不能为空")
    @Size(max = 128, message = "商铺名称长度超限")
    private String name;

        /**
        * 商铺类型的id
        */
    @TableField("type_id")
    @NotNull(message = "商铺类型的id不能为空")
    private Long typeId;

        /**
        * 商铺图片，多个图片以','隔开
        */
    @TableField("images")
    @Size(max = 1024, message = "商铺图片，多个图片以','隔开长度超限")
    private String images;

        /**
        * 商圈，例如陆家嘴
        */
    @TableField("area")
    @Size(max = 128, message = "商圈，例如陆家嘴长度超限")
    private String area;

        /**
        * 地址
        */
    @TableField("address")
    @Size(max = 255, message = "地址长度超限")
    private String address;

        /**
        * 经度
        */
    @TableField("x")
    @NotNull(message = "经度不能为空")
    private Double x;

        /**
        * 维度
        */
    @TableField("y")
    @NotNull(message = "维度不能为空")
    private Double y;

        /**
        * 均价，取整数
        */
    @TableField("avg_price")
    private Long avgPrice;

        /**
        * 销量
        */
    @TableField("sold")
    private Integer sold;

        /**
        * 评论数量
        */
    @TableField("comments")
    private Integer comments;

        /**
        * 评分，1~5分，乘10保存，避免小数
        */
    @TableField("score")
    private Integer score;

        /**
        * 营业时间，例如 10:00-22:00
        */
    @TableField("open_hours")
    @Size(max = 32, message = "营业时间，例如 10:00-22:00长度超限")
    private String openHours;

}