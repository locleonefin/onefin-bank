package com.onefin.ewallet.bank.dto.woori;

import com.onefin.ewallet.bank.dto.woori.individual.DirectMessageIndividualBQ001;
import com.onefin.ewallet.bank.dto.woori.individual.DirectMessageIndividualRV002;
import com.onefin.ewallet.bank.dto.woori.individual.DirectMessageIndividualRV004;
import lombok.Data;

import javax.xml.bind.annotation.*;

@Data
public class DirectMessageDto<T> {

	private DirectMessageCommon common;

	private T individual;
}
