package com.yefersoncm.simulador_api.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "simulations")
public class CreditSimulation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private BigDecimal clientIncome;
    private BigDecimal loanAmount;
    private int termMonths;
    private BigDecimal monthlyPayment;
    private double annualInterestRate;
    private double monthlyInterestRate;

    // --- NUEVO CAMPO ---
    private double debtCapacityPercentage;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime simulationDate;

    @OneToMany(mappedBy = "simulation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AmortizationEntry> schedule;
}