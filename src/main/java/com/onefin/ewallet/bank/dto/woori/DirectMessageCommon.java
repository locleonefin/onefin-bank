package com.onefin.ewallet.bank.dto.woori;

import com.onefin.ewallet.common.base.anotation.MinMaxFieldLengthConstraint;
import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Common")
public class DirectMessageCommon {

	@MinMaxFieldLengthConstraint(maxLength = 6)
	private String msgLn;

	@MinMaxFieldLengthConstraint(maxLength = 9)
	private String coNo;

	@MinMaxFieldLengthConstraint(maxLength = 5)
	private String msgDscd;

	@MinMaxFieldLengthConstraint(maxLength = 3)
	private String reqRsqDscd;

	@MinMaxFieldLengthConstraint(maxLength = 6)
	private String msgNo;

	@MinMaxFieldLengthConstraint(maxLength = 8)
	private String tmsDt;

	@MinMaxFieldLengthConstraint(maxLength = 6)
	private String tmsTm;

	@MinMaxFieldLengthConstraint(maxLength = 4)
	private String rspCd;

	@MinMaxFieldLengthConstraint(maxLength = 9)
	private String idCd;

	@MinMaxFieldLengthConstraint(maxLength = 4)
	private String dataCnt;

	@MinMaxFieldLengthConstraint(maxLength = 16)
	private String etcAr;

}
