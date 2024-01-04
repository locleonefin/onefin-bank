package com.onefin.ewallet.bank.dto.vietin;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
public class VietinVirtualAcctUpdateStatus {

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

  private String newStatus;

  private String clientIP;

  private String transTime;

  public VietinVirtualAcctUpdateStatus() {
  }
}
