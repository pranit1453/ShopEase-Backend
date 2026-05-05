package com.shopease.payment.details;

import lombok.Data;

@Data
public class NetBankingDetails implements PaymentDetails {
    private String bankCode;
    private String accountNumber;
}
