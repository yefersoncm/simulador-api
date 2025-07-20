package com.yefersoncm.simulador_api.dto;

import com.yefersoncm.simulador_api.model.AmortizationEntry;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para formatear la respuesta JSON de una simulación de crédito exitosa.
 * Incluye tanto la tasa anual como la mensual.
 */
@Data
public class CreditResponseDTO {

    private Integer id;
    private BigDecimal clientIncome;
    private BigDecimal loanAmount;
    private int termMonths;
    private BigDecimal monthlyPayment;
    private LocalDateTime simulationDate;

    // --- CAMPOS REQUERIDOS ---
    private double annualInterestRate;
    private double monthlyInterestRate;

    private List<AmortizationEntry> schedule;
}