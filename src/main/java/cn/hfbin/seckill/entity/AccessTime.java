package cn.hfbin.seckill.entity;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AccessTime {
	private int id;
	private Date time;
}
