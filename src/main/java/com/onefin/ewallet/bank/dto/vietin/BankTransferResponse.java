package com.onefin.ewallet.bank.dto.vietin;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.onefin.ewallet.common.domain.bank.transfer.BankTransferChildRecords;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class BankTransferResponse {

	private String requestId;

	private String providerId;

	private String merchantId;

	private Status status = new Status();

	private String processedRecord;

	private String accountName;

	private String bankId;

	private String branchId;

	private String bankName;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String signature;

	private List<BankTransferRecordResponse> records = new ArrayList<>();

	private BankTransferBalances balances;

	private String bankCode;

	public void addRecordList(List<BankTransferRecordResponse> listAdd) {
		if (records == null) {
			records = new ArrayList<>();
		}
		records.addAll(listAdd);
	}

}
