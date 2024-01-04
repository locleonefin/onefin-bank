package com.onefin.ewallet.bank.dto.bankCollection;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateVirtualAcctRequest {

	private String status;

	private BigDecimal amount;

	private String virtualAcctId;

	private String virtualAcctName;

	private String partnerCode;

	private String merchantCode;


}
