package com.onefin.ewallet.bank.dto.vcb;

import lombok.Data;

@Data
public class CardTokenPayment {

	private String number;

	private int expire_month;

	private int expire_year;

	private int cvv;

	private String pay_time;

	private int sequence_number;
}
