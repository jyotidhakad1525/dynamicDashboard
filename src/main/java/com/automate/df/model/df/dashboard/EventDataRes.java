package com.automate.df.model.df.dashboard;

import java.math.BigDecimal;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EventDataRes {
	String id;
	String eventName;
	String eventDate;
	BigDecimal budget;
	Long C;
	Long contPerCar;
	Long enqPerCar;
	Long bkgPerCar;
	Long E;
	Long T;
	Long V;
	Long B;
	Long R;
	Long L;


}
