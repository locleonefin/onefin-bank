package com.onefin.ewallet.bank.dto.vcb;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class Vcb2EWalletResponse extends DataDecryptBaseResponse {

    private Vcb2EWalletUserDetailResponse data;

}
