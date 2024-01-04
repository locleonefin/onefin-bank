package com.onefin.ewallet.bank.dto.bvb;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.onefin.ewallet.common.base.constants.OneFinConstants;
import lombok.Data;

@Data
public class BVBTestUnitRequest extends BVBVirtualAcctCommonRequest {

	@JsonProperty("msgId")
	private String msgId;

	@JsonProperty("lang")
	private String lang = OneFinConstants.LANGUAGE.VIETNAMESE.getValue();

	@JsonProperty("data")
	private BVBTestUnitRequestData data;

	public BVBTestUnitRequest() {
	}

	@Override
	public String toString() {
		return "BVBTestUnitRequest{" +
				"msgId='" + msgId + '\'' +
				", lang='" + lang + '\'' +
				", data=" + data + "," + super.toString() +
				'}';
	}
}
