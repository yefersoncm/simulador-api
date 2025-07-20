package com.yefersoncm.simulador_api.controller;

import com.yefersoncm.simulador_api.dto.CreditRequestDTO;
import com.yefersoncm.simulador_api.dto.CreditResponseDTO; // <-- IMPORT NUEVO
import com.yefersoncm.simulador_api.model.AmortizationEntry;
import com.yefersoncm.simulador_api.model.CreditSimulation;
import com.yefersoncm.simulador_api.service.CreditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/credits")
public class CreditController {

    private final CreditService creditService;

    @Autowired
    public CreditController(CreditService creditService) {
        this.creditService = creditService;
    }

    @PostMapping("/simulate")
    public ResponseEntity<?> simulateCredit(@RequestBody CreditRequestDTO requestDTO) {
        try {
            CreditSimulation simulationResult = creditService.processAndSaveSimulation(requestDTO);

            if (simulationResult == null) {
                return ResponseEntity.badRequest().body("Crédito no viable: la cuota mensual excede el 30% del ingreso.");
            }

            // --- TRANSFORMACIÓN A DTO DE RESPUESTA ---
            CreditResponseDTO responseDTO = buildResponseDTO(simulationResult);

            return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ocurrió un error inesperado.");
        }
    }
    /**
     * Endpoint para obtener el plan de pagos de una simulación existente de forma paginada.
     *
     * @param simulationId El ID de la simulación a consultar.
     * @param page El número de página (inicia en 0).
     * @param size El número de cuotas por página.
     * @return Una página de resultados del plan de pagos.
     */
    @GetMapping("/{simulationId}/schedule")
    public ResponseEntity<Page<AmortizationEntry>> getSchedule(
            @PathVariable Integer simulationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        Page<AmortizationEntry> schedulePage = creditService.getScheduleForSimulation(simulationId, page, size);
        return ResponseEntity.ok(schedulePage);
    }

    /**
     * Método ayudante para construir el DTO de respuesta a partir de la entidad de simulación.
     * @param simulation La entidad guardada en la base de datos.
     * @return El DTO listo para ser enviado como JSON.
     */
    private CreditResponseDTO buildResponseDTO(CreditSimulation simulation) {
        CreditResponseDTO dto = new CreditResponseDTO();

        // Copiar todos los campos de la entidad al DTO
        dto.setId(simulation.getId());
        dto.setClientIncome(simulation.getClientIncome());
        dto.setLoanAmount(simulation.getLoanAmount());
        dto.setTermMonths(simulation.getTermMonths());
        dto.setMonthlyPayment(simulation.getMonthlyPayment());
        dto.setSimulationDate(simulation.getSimulationDate());
        dto.setSchedule(simulation.getSchedule());

        // --- LÓGICA PARA INCLUIR AMBAS TASAS ---
        double annualRate = simulation.getAnnualInterestRate();
        dto.setAnnualInterestRate(annualRate);

        // Calcular la tasa mensual a partir de la anual para mostrarla
        double monthlyRateDecimal = Math.pow(1 + (annualRate / 100.0), 1.0 / 12.0) - 1;
        double monthlyRatePercent = monthlyRateDecimal * 100;
        dto.setMonthlyInterestRate(monthlyRatePercent);

        return dto;
    }
}