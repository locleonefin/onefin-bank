package com.onefin.ewallet.bank.dto.bvb.bankTransfer;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class BVBIBFTQueryStatusRequestData {


	@NotNull(message = "refRequestID cannot be null")
	@JsonProperty("refRequestID")
	private String refRequestId;

	private String description;

	@JsonProperty("extraInfo")
	private Object extraInfo;


}
