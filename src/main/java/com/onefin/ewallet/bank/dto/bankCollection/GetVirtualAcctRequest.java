package com.onefin.ewallet.bank.dto.bankCollection;

import com.onefin.ewallet.common.base.anotation.MinMaxFieldLengthConstraint;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class GetVirtualAcctRequest {

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

	@MinMaxFieldLengthConstraint(message = "virtualAcctId must not be empty")
	private String virtualAcctId;

}
