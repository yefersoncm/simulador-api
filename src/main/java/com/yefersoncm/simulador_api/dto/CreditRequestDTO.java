package com.yefersoncm.simulador_api.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreditRequestDTO {

    private BigDecimal clientIncome;
    private BigDecimal loanAmount;
    private int termMonths;

    private Double annualInterestRate;
    private Double monthlyInterestRate;

    // --- NUEVO CAMPO OPCIONAL ---
    private Double debtCapacityPercentage;
}