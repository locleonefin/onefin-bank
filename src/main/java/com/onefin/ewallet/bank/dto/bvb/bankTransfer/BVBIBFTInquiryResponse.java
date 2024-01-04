package com.onefin.ewallet.bank.dto.bvb.bankTransfer;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class BVBIBFTInquiryResponse extends BVBIBFTCommonResponse<BVBIBFTInquiryResponseData> {


	@Override
	public String toString() {
		return "BVBIBFTInquiryResponse{" +
				"responseId='" + responseId + '\'' +
				", status='" + status + '\'' +
				", errorCode='" + errorCode + '\'' +
				", errorMessage='" + errorMessage + '\'' +
				", sig='" + sig + '\'' +
				", data=" + data +
				'}';
	}
}
