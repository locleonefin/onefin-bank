package com.onefin.ewallet.bank.dto.vietin;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class BankTransferList {

	private int id;

	@NotEmpty
	private String citad;

	@NotEmpty
	private String branchCode;

	@NotEmpty
	private String province;

	@NotEmpty
	private String branchName;

}
