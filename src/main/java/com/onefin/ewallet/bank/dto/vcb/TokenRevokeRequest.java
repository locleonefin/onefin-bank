package com.onefin.ewallet.bank.dto.vcb;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
public class TokenRevokeRequest {

	@NotEmpty(message = "Not empty tokenId")
	private String tokenId;

	@Size(max = 2)
	@NotEmpty(message = "Not empty lang")
	private String lang;

}
