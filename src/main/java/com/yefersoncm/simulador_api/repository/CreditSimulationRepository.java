package com.yefersoncm.simulador_api.repository;

import com.yefersoncm.simulador_api.model.CreditSimulation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para la entidad CreditSimulation.
 * Extiende JpaRepository para obtener automáticamente los métodos CRUD (Crear, Leer, Actualizar, Borrar)
 * y otras operaciones de base de datos para la entidad CreditSimulation.
 */
@Repository
public interface CreditSimulationRepository extends JpaRepository<CreditSimulation, Integer> {

    // Spring Data JPA implementará automáticamente los métodos como:
    // save(), findById(), findAll(), deleteById(), etc.
    // No es necesario añadir nada aquí para las operaciones básicas.

}