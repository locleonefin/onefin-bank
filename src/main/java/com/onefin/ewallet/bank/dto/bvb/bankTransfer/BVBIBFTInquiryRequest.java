package com.onefin.ewallet.bank.dto.bvb.bankTransfer;


import lombok.Data;


@Data
public class BVBIBFTInquiryRequest extends BVBIBFTCommonRequest<BVBIBFTInquiryRequestData> {
	@Override
	public String toString() {
		return "BVBIBFTInquiryRequest{" +
				"requestId='" + requestId + '\'' +
				", clientCode='" + clientCode + '\'' +
				", clientUserId='" + clientUserId + '\'' +
				", time=" + time +
				", signature='" + signature + '\'' +
				", data=" + data +
				'}';
	}
}
