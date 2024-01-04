package com.onefin.ewallet.bank.dto.bvb.bankTransfer;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class BVBIBFTInquiryResponseData {
	@NotNull(message = "full name cannot be null")
	@JsonProperty("fullname")
	private String fullName;
}
