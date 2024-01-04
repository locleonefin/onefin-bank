package com.onefin.ewallet.bank.dto.bvb.bankTransfer;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

@EqualsAndHashCode(callSuper = true)
@Data
public class BVBIBFTFundTransferResponse extends BVBIBFTCommonResponse<BVBIBFTFundTransferResponseData> {

	@Override
	public String toString() {
		return "BVBIBFTFundTransferResponse{" +
				"responseId='" + responseId + '\'' +
				", status='" + status + '\'' +
				", errorCode='" + errorCode + '\'' +
				", errorMessage='" + errorMessage + '\'' +
				", sig='" + sig + '\'' +
				", data=" + data +
				'}';
	}
}
