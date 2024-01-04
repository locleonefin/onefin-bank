package com.onefin.ewallet.bank.dto.vietin;

import lombok.Data;

import javax.persistence.Column;
import javax.validation.constraints.*;
import java.math.BigDecimal;

@Data
public class VirtualAcctGetRequest {

	@NotEmpty(message = "pool not be empty")
	private String pool;

	@NotEmpty(message = "transUniqueKey not be empty")
	private String transUniqueKey;

	@NotEmpty(message = "merchantCode not be empty")
	private String merchantCode;

//	@NotEmpty(message = "partner not be empty")
//	private String partner;

	@DecimalMin(value = "0.0", inclusive = false)
	@NotNull
	private BigDecimal amount;

	@NotEmpty(message = "backendUrl not be empty")
	private String backendUrl;

	private String remark;

	@NotEmpty(message = "bankCode cannot be empty")
	@NotNull(message = "bankCode cannot be null")
	private String bankCode;

}
