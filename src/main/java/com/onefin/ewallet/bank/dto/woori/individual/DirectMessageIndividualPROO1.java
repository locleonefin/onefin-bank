package com.onefin.ewallet.bank.dto.woori.individual;

import com.onefin.ewallet.common.base.anotation.MinMaxFieldLengthConstraint;
import lombok.Data;

@Data
public class DirectMessageIndividualPROO1 {

	@MinMaxFieldLengthConstraint(maxLength = 25)
	private String wdracNo;

	@MinMaxFieldLengthConstraint(maxLength = 8)
	private String actPwno;

	@MinMaxFieldLengthConstraint(maxLength = 6)
	private String rercdMrk;

	@MinMaxFieldLengthConstraint(maxLength = 23)
	private String wdrAm;

	@MinMaxFieldLengthConstraint(maxLength = 1)
	private String wdrAfBlMrk;

	@MinMaxFieldLengthConstraint(maxLength = 23)
	private String wdrAfBl;

	@MinMaxFieldLengthConstraint(maxLength = 1)
	private String tobkDscd;

	@MinMaxFieldLengthConstraint(maxLength = 1)
	private String istDscd;

	@MinMaxFieldLengthConstraint(maxLength = 3)
	private String curCd;

	@MinMaxFieldLengthConstraint(maxLength = 1)
	private String inCdAccGb;

	@MinMaxFieldLengthConstraint(maxLength = 8)
	private String rcvbk1Cd;

	@MinMaxFieldLengthConstraint(maxLength = 8)
	private String rcvbk2Cd;

	@MinMaxFieldLengthConstraint(maxLength = 140)
	private String rcvbkNm;

	@MinMaxFieldLengthConstraint(maxLength = 210)
	private String rmk;

	@MinMaxFieldLengthConstraint(maxLength = 25)
	private String rcvacNo;

	@MinMaxFieldLengthConstraint(maxLength = 23)
	private String fee;

	@MinMaxFieldLengthConstraint(maxLength = 69)
	private String rcvacDppeNm;

	@MinMaxFieldLengthConstraint(maxLength = 69)
	private String rspDppeNm;

	@MinMaxFieldLengthConstraint(maxLength = 4)
	private String status;

	@MinMaxFieldLengthConstraint(maxLength = 16)
	private String trnSrno;

	@MinMaxFieldLengthConstraint(maxLength = 8)
	private String trnDt;

	@MinMaxFieldLengthConstraint(maxLength = 9)
	private String depCoNo;

	@MinMaxFieldLengthConstraint(maxLength = 1)
	private String feePreOcc;

	@MinMaxFieldLengthConstraint(maxLength = 1)
	private String feeInclYn;

	@MinMaxFieldLengthConstraint(maxLength = 69)
	private String sndName;

	@MinMaxFieldLengthConstraint(maxLength = 3)
	private String wdrCurCd;

	@MinMaxFieldLengthConstraint(maxLength = 3)
	private String rcvCurCd;

	@MinMaxFieldLengthConstraint(maxLength = 23)
	private String exRate;

	@MinMaxFieldLengthConstraint(maxLength = 23)
	private String exWdrAm;

	@MinMaxFieldLengthConstraint(maxLength = 23)
	private String exRcvAm;

	@MinMaxFieldLengthConstraint(maxLength = 43)
	private String filler;
}
