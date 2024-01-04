package com.onefin.ewallet.bank.dto.vcb;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class CardInstrumentAuthorizationOtp {

	@NotEmpty(message = "Not empty otp")
	private String otp;

}
