package com.onefin.ewallet.bank.dto.woori.individual;

import com.onefin.ewallet.common.base.anotation.MinMaxFieldLengthConstraint;
import lombok.Data;

@Data
public class DirectMessageIndividualFX001 {

	// Applicant information
	@MinMaxFieldLengthConstraint(maxLength = 50)
	private String outActNo;

	@MinMaxFieldLengthConstraint(maxLength = 14)
	private String remitReqMsgNo;

	@MinMaxFieldLengthConstraint(maxLength = 3)
	private String remitCurCd;

	@MinMaxFieldLengthConstraint(maxLength = 23)
	private String remitAmt;

	@MinMaxFieldLengthConstraint(maxLength = 2)
	private String remitMstDscd;

	@MinMaxFieldLengthConstraint(maxLength = 200)
	private String senderName;

	@MinMaxFieldLengthConstraint(maxLength = 1)
	private String remitRsdYn;

	@MinMaxFieldLengthConstraint(maxLength = 2)
	private String remitNatCd;

	@MinMaxFieldLengthConstraint(maxLength = 50)
	private String remitTelNo;

	@MinMaxFieldLengthConstraint(maxLength = 50)
	private String remitPaspNo;

	// Beneficiary Information
	@MinMaxFieldLengthConstraint(maxLength = 1)
	private String remitPsnCoDscd;

	@MinMaxFieldLengthConstraint(maxLength = 200)
	private String receiverName;

	@MinMaxFieldLengthConstraint(maxLength = 100)
	private String receiverFname;

	@MinMaxFieldLengthConstraint(maxLength = 100)
	private String receiverLname;

	@MinMaxFieldLengthConstraint(maxLength = 50)
	private String inActNo;

	@MinMaxFieldLengthConstraint(maxLength = 100)
	private String receiverAdCity;

	@MinMaxFieldLengthConstraint(maxLength = 100)
	private String receiverAdAdmGg;

	@MinMaxFieldLengthConstraint(maxLength = 300)
	private String receiveraddr;

	@MinMaxFieldLengthConstraint(maxLength = 2)
	private String remitnation;

	@MinMaxFieldLengthConstraint(maxLength = 1)
	private String receiverRsdYn;

	@MinMaxFieldLengthConstraint(maxLength = 100)
	private String inEmail1;

	@MinMaxFieldLengthConstraint(maxLength = 300)
	private String remitpurpose;

	@MinMaxFieldLengthConstraint(maxLength = 11)
	private String swiftCd;

	@MinMaxFieldLengthConstraint(maxLength = 200)
	private String inotherbr;

	@MinMaxFieldLengthConstraint(maxLength = 300)
	private String inotheraddr;

	@MinMaxFieldLengthConstraint(maxLength = 3)
	private String feeCharge;

	@MinMaxFieldLengthConstraint(maxLength = 1)
	private String ovsRmtYn;

	@MinMaxFieldLengthConstraint(maxLength = 1)
	private String payResMsg;

	@MinMaxFieldLengthConstraint(maxLength = 400)
	private String rmk;

	@MinMaxFieldLengthConstraint(maxLength = 136)
	private String filler;


}
