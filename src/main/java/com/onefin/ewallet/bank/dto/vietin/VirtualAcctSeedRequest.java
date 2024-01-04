package com.onefin.ewallet.bank.dto.vietin;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class VirtualAcctSeedRequest {

	@NotEmpty(message = "prefix not be empty")
	private String prefix;

	@NotEmpty(message = "pool not be empty")
	private String pool;

	private int virtualAccPoolInitSize;

	@NotEmpty(message = "virtualAccPoolAccountName not be empty")
	private String virtualAccPoolAccountName;

	@NotEmpty(message = "bankCode cannot be empty")
	@NotNull(message = "bankCode cannot be null")
	private String bankCode;

}
