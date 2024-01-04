package com.onefin.ewallet.bank.dto.bvb.bankTransfer;

import lombok.Data;

import java.io.Serializable;

@Data
public class BVBIBFTFundTransferRequest extends BVBIBFTCommonRequest<BVBIBFTFundTransferRequestData> implements Serializable {

	@Override
	public String toString() {
		return "BVBIBFTFundTransferRequest{" +
				"requestId='" + requestId + '\'' +
				", clientCode='" + clientCode + '\'' +
				", clientUserId='" + clientUserId + '\'' +
				", time=" + time +
				", signature='" + signature + '\'' +
				", data=" + data +
				'}';
	}
}
