package com.onefin.ewallet.bank.dto.vietin;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
public class VietinVirtualAcctCreate {

  @Size(max = 12)
  @NotEmpty(message = "Not empty")
  private String requestId;

  @Size(max = 15)
  @NotEmpty(message = "Not empty")
  private String channel;

  @Size(max = 2)
  private String language;

  private String providerId;

  private String merchantId;

  private String version;

  private String signature;

  private String acctId;

  private String virtualAcctCode;

  private String virtualAcctVar;

  private String maxCredit = "";

  private String minCredit = "";

  private String creditExpireDate = "";

  private String debitExpireDate = "";

  private String maxDebit = "";

  private String minDebit = "";

  private String effectiveDate = "";

  private String expireDate = "";

  private String checkerId = "";

  private String makerId = "";

  private String virtualAcctName = "";

  private String virtualAcctStatus;

  private String isManagedByVTB;

  private String productCode = "";

  private String productName = "";

  private String customerCode = "";

  private String customerName = "";

  private String clientIP;

  private String transTime;

  public VietinVirtualAcctCreate() {
  }
}
