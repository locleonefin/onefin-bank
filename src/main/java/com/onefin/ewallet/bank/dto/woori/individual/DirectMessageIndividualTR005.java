package com.onefin.ewallet.bank.dto.woori.individual;

import com.onefin.ewallet.common.base.anotation.MinMaxFieldLengthConstraint;
import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Individual")
public class DirectMessageIndividualTR005 {
	@MinMaxFieldLengthConstraint(maxLength = 25)
	private String reqActNo = "";

	@MinMaxFieldLengthConstraint(maxLength = 8)
	private String reqTrnDt = "";

	@MinMaxFieldLengthConstraint(maxLength = 3)
	private String reqCurCd = "";

	@MinMaxFieldLengthConstraint(maxLength = 23)
	private String reqTrnAm = "";

	@MinMaxFieldLengthConstraint(maxLength = 5)
	private String orgMsgDscd = "";

	@MinMaxFieldLengthConstraint(maxLength = 6)
	private String orgMsgNo = "";

	@MinMaxFieldLengthConstraint(maxLength = 5)
	private String status = "";
}
