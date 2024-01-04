package com.onefin.ewallet.bank.dto.woori;

import com.onefin.ewallet.bank.dto.woori.individual.DirectMessageIndividualPROO1;
import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Data
@XmlRootElement(name = "Transaction")
@XmlAccessorType(XmlAccessType.FIELD)
public class DirectMessagePROO1 {

	@XmlElement(name = "Common")
	private DirectMessageCommon common;

	@XmlElement(name = "Individual")
	private DirectMessageIndividualPROO1 individual;

}
