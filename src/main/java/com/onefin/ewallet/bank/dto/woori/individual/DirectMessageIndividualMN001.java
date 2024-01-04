package com.onefin.ewallet.bank.dto.woori.individual;

import com.onefin.ewallet.common.base.anotation.MinMaxFieldLengthConstraint;
import lombok.Data;

@Data
public class DirectMessageIndividualMN001 {

	@MinMaxFieldLengthConstraint(maxLength = 25)
	private String actNo;

	@MinMaxFieldLengthConstraint(maxLength = 3)
	private String curCd;

	@MinMaxFieldLengthConstraint(maxLength = 2)
	private String rpDscd;

	@MinMaxFieldLengthConstraint(maxLength = 1)
	private String trnDscd;

	@MinMaxFieldLengthConstraint(maxLength = 23)
	private String trnAm;

	@MinMaxFieldLengthConstraint(maxLength = 1)
	private String trnAfBlMrk;

	@MinMaxFieldLengthConstraint(maxLength = 23)
	private String trnAfBl;

	@MinMaxFieldLengthConstraint(maxLength = 69)
	private String rqpeNm;

	@MinMaxFieldLengthConstraint(maxLength = 8)
	private String trnDt;

	@MinMaxFieldLengthConstraint(maxLength = 6)
	private String trnTm;

	@MinMaxFieldLengthConstraint(maxLength = 25)
	private String rqpeActNo;

	@MinMaxFieldLengthConstraint(maxLength = 7)
	private String trnSrno;

	@MinMaxFieldLengthConstraint(maxLength = 25)
	private String virActNo;

	@MinMaxFieldLengthConstraint(maxLength = 210)
	private String rmk;

	@MinMaxFieldLengthConstraint(maxLength = 150)
	private String pbprtTxt;

	@MinMaxFieldLengthConstraint(maxLength = 100)
	private String coUseFld;

	@MinMaxFieldLengthConstraint(maxLength = 196)
	private String filler;

}
