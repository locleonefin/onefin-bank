package com.onefin.ewallet.bank.dto.woori.individual;

import com.onefin.ewallet.common.base.anotation.MinMaxFieldLengthConstraint;
import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Individual")
public class DirectMessageIndividualBQ001 {

	@MinMaxFieldLengthConstraint(maxLength = 25)
	private String actNo = "";

	@MinMaxFieldLengthConstraint(maxLength = 1)
	private String tobkDscd = "";

	@MinMaxFieldLengthConstraint(maxLength = 1)
	private String istDscd = "";

	@MinMaxFieldLengthConstraint(maxLength = 3)
	private String curCd = "";

	@MinMaxFieldLengthConstraint(maxLength = 11)
	private String rcpbkCd = "";

	@MinMaxFieldLengthConstraint(maxLength = 1)
	private String balBlMrk = "";

	@MinMaxFieldLengthConstraint(maxLength = 23)
	private String ldgBl = "";

	@MinMaxFieldLengthConstraint(maxLength = 23)
	private String cshtfAm = "";

	@MinMaxFieldLengthConstraint(maxLength = 23)
	private String payHcHkAm = "";

	@MinMaxFieldLengthConstraint(maxLength = 23)
	private String etcObrAm = "";

	@MinMaxFieldLengthConstraint(maxLength = 23)
	private String rlPayAvlAm = "";

	@MinMaxFieldLengthConstraint(maxLength = 567)
	private String filler = "";
}
