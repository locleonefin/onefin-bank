package com.onefin.ewallet.bank.dto.vietin;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
public class CreateVirtualAcctTemp {

  @Size(max = 20)
  @NotEmpty(message = "Not empty")
  private String queryTransactionId;

  @Size(max = 6)
  @NotEmpty(message = "Not empty")
  private String queryType;

  @Size(max = 12)
  @NotEmpty(message = "Not empty")
  private String requestId;

  @Size(max = 15)
  @NotEmpty(message = "Not empty")
  private String channel;

  @Size(max = 3)
  private String language;

  @Size(max = 30)
  private String mac;

  private String providerId;

  private String merchantId;

  private String version;

  private String signature;

  private String acctId;

  private String virtualAcctCode;

  private String virtualAcctVar;

  private String maxCredit;

  private String minCredit;

  private String creditExpireDate;

  private String debitExpireDate;

  private String maxDebit;

  private String minDebit;

  private String effectiveDate;

  private String expireDate;

  private String checkerId;

  private String makerId;

  private String virtualAcctName;

  private String virtualAcctStatus;

  private String isManagedByVTB;

  private String productCode;

  private String productName;

  private String customerCode;

  private String customerName;

  private String clientIP;

  private String transTime;

  public CreateVirtualAcctTemp() {
    this.language = new String();
    this.mac = new String();
  }
}
