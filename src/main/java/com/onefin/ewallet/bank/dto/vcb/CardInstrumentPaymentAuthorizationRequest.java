package com.onefin.ewallet.bank.dto.vcb;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
public class CardInstrumentPaymentAuthorizationRequest {

	@NotEmpty(message = "Not empty verifyTransactionId")
	private String verifyTransactionId;

	private CardInstrumentAuthorizationOtp authorization_input;

	@JsonProperty("authorizationInput")
	public void setAuthorizationInput(CardInstrumentAuthorizationOtp authorizationInput) {
		if (authorizationInput != null) {
			this.authorization_input = authorizationInput;
		}
	}

	@Size(max = 2)
	@NotEmpty(message = "Not empty lang")
	private String lang;

}
