package com.onefin.ewallet.bank.dto.vcb;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CardPaymentAuthorizationResponse extends ErrorResponse {

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String id;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String type;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String state;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String create_time;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String update_time;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private CardInstrumentAuthorizationInstrumentResponse instrument;

	private String tokenId;

	@JsonProperty("instrument")
	public void setInstrument(CardInstrumentAuthorizationInstrumentResponse instrument) {
		if (instrument != null) {
			this.tokenId = instrument.getToken().getId();
			this.instrument = instrument;
		}
	}
}
