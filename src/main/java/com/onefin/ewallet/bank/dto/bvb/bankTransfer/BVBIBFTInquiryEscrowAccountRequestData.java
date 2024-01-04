package com.onefin.ewallet.bank.dto.bvb.bankTransfer;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class BVBIBFTInquiryEscrowAccountRequestData {

	@NotNull(message = "accountNo cannot be null")
	@JsonProperty("accountNo")
	private String accountNo;
}
