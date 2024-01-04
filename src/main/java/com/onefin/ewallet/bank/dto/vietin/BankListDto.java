package com.onefin.ewallet.bank.dto.vietin;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.onefin.ewallet.common.domain.constants.DomainConstants;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class BankListDto {

	private String code; // citad

	private String name;

	private String bankCode;

	private Boolean headquarters;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DomainConstants.DATE_FORMAT_TRANS, timezone = DomainConstants.HO_CHI_MINH_TIME_ZONE)
	private Date createdDate;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DomainConstants.DATE_FORMAT_TRANS, timezone = DomainConstants.HO_CHI_MINH_TIME_ZONE)
	private Date updatedDate;

	private List<String> napasBin;

}
