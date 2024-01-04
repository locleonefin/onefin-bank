package com.onefin.ewallet.bank.dto.vietin;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;

@Data
public class VirtualAcctCreateRequest {

	@Size(max = 12)
	@NotEmpty(message = "requestIdmust not be empty")
	@NotNull(message = "virtualAcctName Not Null")
	private String requestId;

	@NotNull()
	@Pattern(regexp = "[0-9]*", message = "maxCredit must be number")
	private String maxCredit = "";

	@NotNull()
	@Pattern(regexp = "[0-9]*", message = "minCredit must be number")
	private String minCredit = "";

	@NotNull()
	private String creditExpireDate = "";

	@NotNull()
	private String effectiveDate = "";

	@NotNull()
	private String expireDate = "";

	@NotNull()
	@NotEmpty(message = "virtualAcctName must not be empty")
	private String virtualAcctName;

	@NotNull()
	@NotEmpty(message = "virtualAcctVar must not be empty")
	@Pattern(regexp = "[A-Za-z0-9]*")
	private String virtualAcctVar;

	@NotNull()
	private String productCode = "";

	@NotNull()
	private String productName = "";

	@NotNull()
	private String customerCode = "";

	@NotNull()
	private String customerName = "";

	@Size(max = 2)
	@Pattern(regexp = "en|vi", message = "language Must be en or vi")
	private String language = "vi";

	private String poolName;

	public VirtualAcctCreateRequest() {
	}

}
