package com.onefin.ewallet.bank.dto.woori.individual;

import com.onefin.ewallet.common.base.anotation.MinMaxFieldLengthConstraint;
import lombok.Data;

@Data
public class DirectMessageIndividualTR001 {

	@MinMaxFieldLengthConstraint(maxLength = 25)
	private String actNo;

	@MinMaxFieldLengthConstraint(maxLength = 1)
	private String tobkDscd;

	@MinMaxFieldLengthConstraint(maxLength = 1)
	private String istDscd;

	@MinMaxFieldLengthConstraint(maxLength = 8)
	private String trnDt;

	@MinMaxFieldLengthConstraint(maxLength = 6)
	private String trnTm;

	@MinMaxFieldLengthConstraint(maxLength = 3)
	private String curCd;

	@MinMaxFieldLengthConstraint(maxLength = 23)
	private String trnAm;

	@MinMaxFieldLengthConstraint(maxLength = 25)
	private String inActNo;

	@MinMaxFieldLengthConstraint(maxLength = 5)
	private String status;

	@MinMaxFieldLengthConstraint(maxLength = 3)
	private String feeCurCd;

	@MinMaxFieldLengthConstraint(maxLength = 23)
	private String feeAm;

	@MinMaxFieldLengthConstraint(maxLength = 69)
	private String recieveName;

	@MinMaxFieldLengthConstraint(maxLength = 210)
	private String outParticular;

	@MinMaxFieldLengthConstraint(maxLength = 210)
	private String inParticular;

	@MinMaxFieldLengthConstraint(maxLength = 16)
	private String trnSrno;

	@MinMaxFieldLengthConstraint(maxLength = 96)
	private String filler;


}
