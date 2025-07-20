package com.yefersoncm.simulador_api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Representa una única entrada (una cuota) en la tabla de amortización.
 * Cada instancia corresponde a un pago mensual específico de una simulación de crédito.
 */
@Data
@Entity
@Table(name = "amortization_schedule")
public class AmortizationEntry {

    /**
     * Identificador único para esta entrada de amortización, generado automáticamente.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * La simulación de crédito a la que pertenece esta entrada.
     * Define la relación "muchos a uno": muchas cuotas pertenecen a una simulación.
     * Se usa FetchType.LAZY como buena práctica para no cargar la simulación completa innecesariamente.
     * Se usa @JsonIgnore para evitar bucles infinitos durante la serialización a JSON.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "simulation_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude // Excluye este campo del método toString() generado por Lombok para evitar recursión.
    private CreditSimulation simulation;

    /**
     * El número de la cuota en la secuencia (ej. 1, 2, 3, ...).
     */
    private int paymentNumber;

    /**
     * La fecha en que se debe realizar el pago de esta cuota.
     */
    private LocalDate paymentDate;

    /**
     * La porción de la cuota que se destina a pagar el capital del préstamo.
     */
    private BigDecimal principalAmount;

    /**
     * La porción de la cuota que se destina a pagar los intereses generados.
     */
    private BigDecimal interestAmount;

    /**
     * El saldo del crédito que queda por pagar después de realizar este pago.
     */
    private BigDecimal remainingBalance;
}