package com.onefin.ewallet.bank.dto.vcb;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CreateWithdrawCardWalletRequest extends CreateWithdrawCardRequest {

	@NotEmpty(message = "Not empty requestId")
	private String requestId;

	@NotEmpty(message = "Not empty tokenId")
	private String tokenId;

	@NotEmpty(message = "Not empty lang")
	private String lang;

}
