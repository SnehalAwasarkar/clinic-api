package com.example.clinic.repository;

import com.example.clinic.entity.Patient;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    @Query("SELECT DISTINCT p FROM Patient p WHERE "
            + "p.firstName ILIKE CONCAT('%', :q, '%') "
            + "OR p.lastName ILIKE CONCAT('%', :q, '%') "
            + "OR TRIM(CONCAT(COALESCE(p.firstName, ''), ' ', COALESCE(p.lastName, ''))) ILIKE CONCAT('%', :q, '%') "
            + "OR p.email ILIKE CONCAT('%', :q, '%') "
            + "OR REPLACE(REPLACE(p.phone, ' ', ''), '-', '') ILIKE CONCAT('%', REPLACE(REPLACE(:q, ' ', ''), '-', ''), '%') "
            + "ORDER BY p.id ASC")
    List<Patient> search(@Param("q") String q);
}
