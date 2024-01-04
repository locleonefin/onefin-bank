package com.onefin.ewallet.bank.dto.vcb;

import lombok.Data;

@Data
public class CardToken {

	private String id;

	private String instrument_id;

	private String number;

	private String icvv;

	private String expire_month;

	private String expire_year;

	private String state;

	private int cvv;

	private String pay_time;

	private int sequence_number;
}
