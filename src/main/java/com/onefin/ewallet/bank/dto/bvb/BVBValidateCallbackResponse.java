package com.onefin.ewallet.bank.dto.bvb;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BVBValidateCallbackResponse extends BVBVirtualAcctCommonResponse {


	@JsonProperty("data")
	BVBCallbackAPITransData data;

	public BVBValidateCallbackResponse() {
	}

	@Override
	public String toString() {
		return "BVBValidateCallbackResponse{" +
				"data=" + data + "," + super.toString() +
				'}';
	}
}
