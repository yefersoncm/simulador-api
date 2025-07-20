package com.yefersoncm.simulador_api.repository;

import com.yefersoncm.simulador_api.model.AmortizationEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AmortizationEntryRepository extends JpaRepository<AmortizationEntry, Integer> {

    /**
     * Busca y devuelve una página de entradas de amortización que pertenecen
     * a una simulación específica, ordenadas por el número de pago.
     *
     * @param simulationId El ID de la simulación padre.
     * @param pageable La información de paginación (número de página y tamaño).
     * @return una página (Page) de AmortizationEntry.
     */
    Page<AmortizationEntry> findBySimulationIdOrderByPaymentNumberAsc(Integer simulationId, Pageable pageable);
}