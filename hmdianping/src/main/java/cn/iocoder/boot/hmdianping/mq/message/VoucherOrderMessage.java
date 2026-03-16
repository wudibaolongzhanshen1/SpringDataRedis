package cn.iocoder.boot.hmdianping.mq.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VoucherOrderMessage {
    private Long voucherId;
    private Long userId;
    private Long orderId;
}
