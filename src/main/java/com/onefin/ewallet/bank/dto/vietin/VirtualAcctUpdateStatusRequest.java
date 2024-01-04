package com.onefin.ewallet.bank.dto.vietin;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class VirtualAcctUpdateStatusRequest {

  @Size(max = 12)
  @NotEmpty(message = "Not empty")
  private String requestId;

  @NotEmpty(message = "Not empty")
  private String virtualAcctVar;

  @NotEmpty()
  @Size(max = 1)
  @Pattern(regexp="[04]", message = "newStatus Must be 0 or 4")
  private String newStatus;

  @Size(max = 2)
  @Pattern(regexp="en|vi", message = "language Must be en or vi")
  private String language = "vi";

  public VirtualAcctUpdateStatusRequest() {
  }

}
