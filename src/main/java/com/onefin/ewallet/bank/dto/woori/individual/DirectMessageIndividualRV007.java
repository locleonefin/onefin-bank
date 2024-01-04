package com.onefin.ewallet.bank.dto.woori.individual;

import com.onefin.ewallet.common.base.anotation.MinMaxFieldLengthConstraint;
import lombok.Data;

@Data
public class DirectMessageIndividualRV007 {

	@MinMaxFieldLengthConstraint(maxLength = 25)
	private String outActNo;

	@MinMaxFieldLengthConstraint(maxLength = 25)
	private String virActNo;

	@MinMaxFieldLengthConstraint(maxLength = 200)
	private String virAcNm;

	@MinMaxFieldLengthConstraint(maxLength = 100)
	private String refNo;

	@MinMaxFieldLengthConstraint(maxLength = 2)
	private String recCodCd;

	@MinMaxFieldLengthConstraint(maxLength = 23)
	private String trnAm;

	@MinMaxFieldLengthConstraint(maxLength = 8)
	private String trnAvlSdt;

	@MinMaxFieldLengthConstraint(maxLength = 8)
	private String trnAvlEdt;

	@MinMaxFieldLengthConstraint(maxLength = 6)
	private String trnAvlStm;

	@MinMaxFieldLengthConstraint(maxLength = 6)
	private String trnAvlEtm;

	@MinMaxFieldLengthConstraint(maxLength = 1)
	private String trnAvlYn;

	@MinMaxFieldLengthConstraint(maxLength = 9)
	private String corpRecCompCode;

	@MinMaxFieldLengthConstraint(maxLength = 321)
	private String filler;

}
