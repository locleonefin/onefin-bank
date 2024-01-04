package com.onefin.ewallet.bank.dto.bvb.bankTransfer;


import lombok.Data;

@Data

public class BVBIBFTQueryStatusResponse extends BVBIBFTCommonResponse<BVBIBFTQueryStatusResponseData> {

	@Override
	public String toString() {
		return "BVBIBFTQueryStatusResponse{" +
				"responseId='" + responseId + '\'' +
				", status='" + status + '\'' +
				", errorCode='" + errorCode + '\'' +
				", errorMessage='" + errorMessage + '\'' +
				", sig='" + sig + '\'' +
				", data='" + data + '\'' +
				'}';
	}
}
