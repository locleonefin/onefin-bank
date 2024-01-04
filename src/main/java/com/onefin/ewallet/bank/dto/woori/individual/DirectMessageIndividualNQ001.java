package com.onefin.ewallet.bank.dto.woori.individual;

import com.onefin.ewallet.common.base.anotation.MinMaxFieldLengthConstraint;
import lombok.Data;

@Data
public class DirectMessageIndividualNQ001 {

	@MinMaxFieldLengthConstraint(maxLength = 25)
	private String wdrActNo;

	@MinMaxFieldLengthConstraint(maxLength = 25)
	private String rcvActNo;

	@MinMaxFieldLengthConstraint(maxLength = 69)
	private String dppeNm;

	@MinMaxFieldLengthConstraint(maxLength = 1)
	private String tobkDscd;

	@MinMaxFieldLengthConstraint(maxLength = 1)
	private String istDscd;

	@MinMaxFieldLengthConstraint(maxLength = 11)
	private String dpBkCd;

	@MinMaxFieldLengthConstraint(maxLength = 1)
	private String inCdAccGb;

	@MinMaxFieldLengthConstraint(maxLength = 591)
	private String filler;
}
