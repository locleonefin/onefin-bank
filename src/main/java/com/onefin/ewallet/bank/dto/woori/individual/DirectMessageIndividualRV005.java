package com.onefin.ewallet.bank.dto.woori.individual;


import com.onefin.ewallet.common.base.anotation.MinMaxFieldLengthConstraint;
import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@Data
@XmlRootElement(name = "Transaction")
@XmlAccessorType(XmlAccessType.FIELD)
public class DirectMessageIndividualRV005 {
	@MinMaxFieldLengthConstraint(maxLength = 8)
	private String trnRefNo = "";

	@MinMaxFieldLengthConstraint(maxLength = 1)
	private String trnType = "";

	@MinMaxFieldLengthConstraint(maxLength = 25)
	private String outActNo = "";

	@MinMaxFieldLengthConstraint(maxLength = 25)
	private String virActNo = "";

	@MinMaxFieldLengthConstraint(maxLength = 200)
	private String virActNm = "";

	@MinMaxFieldLengthConstraint(maxLength = 200)
	private String regCusNm = "";

	@MinMaxFieldLengthConstraint(maxLength = 23)
	private String trnAm = "";

	@MinMaxFieldLengthConstraint(maxLength = 100)
	private String refNo = "";

	@MinMaxFieldLengthConstraint(maxLength = 2)
	private String recCodCd = "";

	@MinMaxFieldLengthConstraint(maxLength = 23)
	private String virTrnAm = "";

	@MinMaxFieldLengthConstraint(maxLength = 8)
	private String trnAvlSdt = "";

	@MinMaxFieldLengthConstraint(maxLength = 8)
	private String trnAvlEdt = "";

	@MinMaxFieldLengthConstraint(maxLength = 6)
	private String trnAvlStm = "";

	@MinMaxFieldLengthConstraint(maxLength = 6)
	private String trnAvlEtm = "";

	@MinMaxFieldLengthConstraint(maxLength = 2)
	private String trnDscd = "";

	@MinMaxFieldLengthConstraint(maxLength = 8)
	private String trnDt = "";

	@MinMaxFieldLengthConstraint(maxLength = 6)
	private String trnTm = "";

	@MinMaxFieldLengthConstraint(maxLength = 8)
	private String errCd = "";

	@MinMaxFieldLengthConstraint(maxLength = 300)
	private String errMsg = "";

	@MinMaxFieldLengthConstraint(maxLength = 1)
	private String apvYn = "";

	@MinMaxFieldLengthConstraint(maxLength = 300)
	private String etcCtt = "";

	@MinMaxFieldLengthConstraint(maxLength = 50)
	private String filler = "";
}
