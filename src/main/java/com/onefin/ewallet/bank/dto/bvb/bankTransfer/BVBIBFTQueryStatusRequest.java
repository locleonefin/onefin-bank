package com.onefin.ewallet.bank.dto.bvb.bankTransfer;


import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class BVBIBFTQueryStatusRequest
		extends BVBIBFTCommonRequest<BVBIBFTQueryStatusRequestData> {

	//	private BVBTransactionCheckRequestData data;

	@Override
	public String toString() {
		return "BVBIBFTQueryStatusRequest{" +
				"requestId='" + requestId + '\'' +
				", clientCode='" + clientCode + '\'' +
				", clientUserId='" + clientUserId + '\'' +
				", time=" + time +
				", signature='" + signature + '\'' +
				", data=" + data +
				'}';
	}
}
