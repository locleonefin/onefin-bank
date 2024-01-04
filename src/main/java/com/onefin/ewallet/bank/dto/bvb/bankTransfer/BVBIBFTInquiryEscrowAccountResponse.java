package com.onefin.ewallet.bank.dto.bvb.bankTransfer;


import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class BVBIBFTInquiryEscrowAccountResponse extends BVBIBFTCommonResponse<BVBIBFTInquiryEscrowAccountResponseData> {

	@Override
	public String toString() {
		return "BVBIBFTInquiryEscrowAccountResponse{" +
				"responseId='" + responseId + '\'' +
				", status='" + status + '\'' +
				", errorCode='" + errorCode + '\'' +
				", errorMessage='" + errorMessage + '\'' +
				", sig='" + sig + '\'' +
				", data=" + data +
				'}';
	}
}
