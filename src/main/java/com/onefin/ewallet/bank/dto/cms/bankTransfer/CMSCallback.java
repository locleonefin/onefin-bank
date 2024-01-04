package com.onefin.ewallet.bank.dto.cms.bankTransfer;

import lombok.Data;

@Data
public class CMSCallback {

	private String signature;

	private CMSCallbackMessage message;
}
