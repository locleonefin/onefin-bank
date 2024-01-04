package com.onefin.ewallet.bank.dto.cms.bankTransfer;

import com.onefin.ewallet.bank.dto.vietin.Status;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class CMSAccountInquiryResponse {

	private String accountName;

	private String bankId;

	private String bankName;

	@NotNull(message = "status can not be null")
	private Status status;
}
