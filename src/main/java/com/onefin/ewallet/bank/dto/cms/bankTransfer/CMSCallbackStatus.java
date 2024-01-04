package com.onefin.ewallet.bank.dto.cms.bankTransfer;

import lombok.Data;

@Data
public class CMSCallbackStatus {

	private String errorCode;

	private String errorMessage;

	private String errorMessageDetail;
}
