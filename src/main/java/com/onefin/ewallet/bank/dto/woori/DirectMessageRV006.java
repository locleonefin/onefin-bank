package com.onefin.ewallet.bank.dto.woori;

import com.onefin.ewallet.bank.dto.woori.individual.DirectMessageIndividualRV004;
import com.onefin.ewallet.bank.dto.woori.individual.DirectMessageIndividualRV006;
import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Data
@XmlRootElement(name = "Transaction")
@XmlAccessorType(XmlAccessType.FIELD)
public class DirectMessageRV006 {

	@XmlElement(name = "Common")
	private DirectMessageCommon common;

	@XmlElement(name = "Individual")
	private DirectMessageIndividualRV006 individual;

}
