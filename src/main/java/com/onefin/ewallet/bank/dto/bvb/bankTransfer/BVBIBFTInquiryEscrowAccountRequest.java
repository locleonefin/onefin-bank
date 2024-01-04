package com.onefin.ewallet.bank.dto.bvb.bankTransfer;


import lombok.Data;

@Data
public class BVBIBFTInquiryEscrowAccountRequest extends BVBIBFTCommonRequest<BVBIBFTInquiryEscrowAccountRequestData> {
	@Override
	public String toString() {
		return "BVBIBFTInquiryEscrowAccountRequest{" +
				"requestId='" + requestId + '\'' +
				", clientCode='" + clientCode + '\'' +
				", clientUserId='" + clientUserId + '\'' +
				", time=" + time +
				", signature='" + signature + '\'' +
				", data=" + data +
				'}';
	}
}
