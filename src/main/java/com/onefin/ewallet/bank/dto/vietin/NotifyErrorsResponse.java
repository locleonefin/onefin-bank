package com.onefin.ewallet.bank.dto.vietin;

import lombok.Data;

@Data
public class NotifyErrorsResponse {

  private String errorCode;

  private String errorDesc;

  public NotifyErrorsResponse(String errorCode, String errorDesc) {
    this.errorCode = errorCode;
    this.errorDesc = errorDesc;
  }

  public NotifyErrorsResponse() {
  }
}
