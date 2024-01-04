package com.onefin.ewallet.bank.dto.bvb;


import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@EqualsAndHashCode(callSuper = true)
@Data
public class BVBVirtualAcctCreateRequestData
		extends BVBVirtualAcctCommonPartnerRequest {


	@Size(min = 1, max = 7, message = "accNoSuffix max length is 7")
	private String accNoSuffix = "";

	@NotEmpty()
	@Size(max = 1)
	@Pattern(regexp = "M|O", message = "accType must be M or O")
	private String accType = "O";

	public BVBVirtualAcctCreateRequestData() {
	}

	@Override
	public String toString() {
		return "BVBVirtualAcctCreateRequestData{" +
				"accNoSuffix='" + accNoSuffix + '\'' +
				", accType='" + accType + '\'' + "," + super.toString() +
				'}';
	}
}
