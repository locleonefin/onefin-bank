package com.onefin.ewallet.bank.dto.vcb;

import lombok.Data;

@Data
public class CreateInstrumentCardRequest {

	private String user_id;

	private String type;

	private String name;

	private String number;

	private String month;

	private String year;

	private AddressUserRegistrationCard billing_address;

}
