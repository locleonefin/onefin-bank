package com.onefin.ewallet.bank.dto.bvb.bankTransfer;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class BVBIBFTFundTransferRequestData {

	@NotNull(message = "accountNo cannot be null")
	private String accountNo;

	@NotNull(message = "cardNo cannot be null")
	private String cardNo;

	@NotNull(message = "bankCode cannot be null")
	private String bankCode;

	@NotNull(message = "onus cannot be null")
	@Size(max = 1)
	private String onus;

	@NotNull(message = "amount cannot be null")
	private String amount;

	@NotNull(message = "feeModel cannot be null")
	private String feeModel;

	private String description;
}
