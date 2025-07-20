package com.yefersoncm.simulador_api.service;

import com.yefersoncm.simulador_api.dto.CreditRequestDTO;
import com.yefersoncm.simulador_api.model.AmortizationEntry;
import com.yefersoncm.simulador_api.model.CreditSimulation;
import com.yefersoncm.simulador_api.repository.CreditSimulationRepository;
import com.yefersoncm.simulador_api.repository.AmortizationEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio que contiene la lógica de negocio para las simulaciones de crédito.
 */
@Service
public class CreditService {

    private final CreditSimulationRepository creditRepository;
    private final AmortizationEntryRepository amortizationEntryRepository;

    /**
     * Constructor para la inyección de dependencias del repositorio.
     * @param creditRepository El repositorio para interactuar con la base de datos.
     */
    @Autowired
    public CreditService(CreditSimulationRepository creditRepository, AmortizationEntryRepository amortizationEntryRepository) {
        this.creditRepository = creditRepository;
        this.amortizationEntryRepository = amortizationEntryRepository;
    }

    // 2. Añade este nuevo método público
    /**
     * Obtiene el plan de pagos de una simulación de forma paginada.
     *
     * @param simulationId El ID de la simulación.
     * @param page El número de página a obtener (empezando desde 0).
     * @param size El tamaño de la página (cuántas cuotas por página).
     * @return Un objeto Page que contiene la lista de cuotas y la información de paginación.
     */
    public Page<AmortizationEntry> getScheduleForSimulation(Integer simulationId, int page, int size) {
        // Crea un objeto Pageable para pasarle al repositorio
        Pageable pageable = PageRequest.of(page, size);

        // Llama al método del nuevo repositorio
        return amortizationEntryRepository.findBySimulationIdOrderByPaymentNumberAsc(simulationId, pageable);
    }

    /**
     * Orquesta todo el proceso de simulación: valida la entrada, calcula la cuota,
     * valida la viabilidad, genera el plan de pagos y guarda el resultado.
     *
     * @param requestDTO El objeto DTO con los datos de entrada del cliente.
     * @return El objeto CreditSimulation completo y persistido si el crédito es viable, o null si no lo es.
     * @throws IllegalArgumentException si los datos de la tasa de interés son inválidos.
     */
    public CreditSimulation processAndSaveSimulation(CreditRequestDTO requestDTO) {

        double debtCapacity = requestDTO.getDebtCapacityPercentage() != null
                ? requestDTO.getDebtCapacityPercentage()
                : 30.0;


        // --- PASO 1: Validar las tasas de interés (una y solo una debe estar presente) ---


        boolean hasAnnualRate = requestDTO.getAnnualInterestRate() != null;
        boolean hasMonthlyRate = requestDTO.getMonthlyInterestRate() != null;

        if (hasAnnualRate == hasMonthlyRate) { // Esto es verdadero si ambas son verdaderas o ambas son falsas
            throw new IllegalArgumentException("Debe proporcionar 'annualInterestRate' o 'monthlyInterestRate', pero no ambos ni ninguno.");
        }

        // --- PASO 2: Calcular la tasa anual efectiva a partir de la entrada ---
        double annualRate;
        double monthlyRatePercent;

        if (hasAnnualRate) {
            annualRate = requestDTO.getAnnualInterestRate();
            // Calcular la tasa mensual a partir de la anual
            double monthlyRateDecimal = Math.pow(1 + (annualRate / 100.0), 1.0 / 12.0) - 1;
            monthlyRatePercent = monthlyRateDecimal * 100;
        } else {
            monthlyRatePercent = requestDTO.getMonthlyInterestRate();
            // Calcular la tasa anual a partir de la mensual
            double monthlyRateDecimal = monthlyRatePercent / 100.0;
            annualRate = (Math.pow(1 + monthlyRateDecimal, 12) - 1) * 100;
        }

        // --- PASO 3: Calcular la cuota y validar la capacidad de pago ---
        BigDecimal monthlyPayment = calculateMonthlyPayment(
                requestDTO.getLoanAmount(),
                annualRate,
                requestDTO.getTermMonths()
        );

        BigDecimal maxAllowedPayment = requestDTO.getClientIncome()
                .multiply(BigDecimal.valueOf(debtCapacity / 100.0));
        if (monthlyPayment.compareTo(maxAllowedPayment) > 0) {
            return null; // Crédito no viable
        }

        // --- PASO 4: Crear la entidad y poblarla con todos los datos ---
        CreditSimulation simulation = new CreditSimulation();
        simulation.setClientIncome(requestDTO.getClientIncome());
        simulation.setLoanAmount(requestDTO.getLoanAmount());
        simulation.setTermMonths(requestDTO.getTermMonths());
        simulation.setMonthlyPayment(monthlyPayment);


        // --- LÓGICA MODIFICADA ---
        // Guardar ambas tasas en la entidad
        simulation.setAnnualInterestRate(annualRate);
        simulation.setMonthlyInterestRate(monthlyRatePercent);

        List<AmortizationEntry> schedule = generateSchedule(simulation);
        simulation.setSchedule(schedule);

        // --- PASO 5: Guardar la simulación completa y devolverla ---
        return creditRepository.save(simulation);
    }

    /**
     * Calcula la cuota mensual fija usando la fórmula de anualidad (sistema francés).
     */
    private BigDecimal calculateMonthlyPayment(BigDecimal loanAmount, double annualInterestRate, int termMonths) {
        double monthlyRateDecimal = Math.pow(1 + (annualInterestRate / 100.0), 1.0 / 12.0) - 1;
        BigDecimal monthlyRate = BigDecimal.valueOf(monthlyRateDecimal);

        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            return loanAmount.divide(BigDecimal.valueOf(termMonths), 2, RoundingMode.HALF_UP);
        }

        BigDecimal ratePlusOne = monthlyRate.add(BigDecimal.ONE);
        BigDecimal ratePowered = ratePlusOne.pow(termMonths);
        BigDecimal numerator = monthlyRate.multiply(ratePowered);
        BigDecimal denominator = ratePowered.subtract(BigDecimal.ONE);

        return loanAmount.multiply(numerator).divide(denominator, 2, RoundingMode.HALF_UP);
    }

    /**
     * Genera la lista completa de cuotas (tabla de amortización) para una simulación.
     */
    private List<AmortizationEntry> generateSchedule(CreditSimulation simulation) {
        List<AmortizationEntry> schedule = new ArrayList<>();
        BigDecimal remainingBalance = simulation.getLoanAmount();
        double monthlyRateDecimal = Math.pow(1 + (simulation.getAnnualInterestRate() / 100.0), 1.0 / 12.0) - 1;
        BigDecimal monthlyRate = BigDecimal.valueOf(monthlyRateDecimal);
        LocalDate paymentDate = LocalDate.now();

        for (int i = 1; i <= simulation.getTermMonths(); i++) {
            paymentDate = paymentDate.plusMonths(1);

            BigDecimal interestAmount = remainingBalance.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal principalAmount = simulation.getMonthlyPayment().subtract(interestAmount);
            remainingBalance = remainingBalance.subtract(principalAmount);

            if (i == simulation.getTermMonths()) {
                principalAmount = principalAmount.add(remainingBalance);
                remainingBalance = BigDecimal.ZERO;
            }

            AmortizationEntry entry = new AmortizationEntry();
            entry.setSimulation(simulation);
            entry.setPaymentNumber(i);
            entry.setPaymentDate(paymentDate);
            entry.setInterestAmount(interestAmount);
            entry.setPrincipalAmount(principalAmount);
            entry.setRemainingBalance(remainingBalance);

            schedule.add(entry);
        }
        return schedule;
    }
}