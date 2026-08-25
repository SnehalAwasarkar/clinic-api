package com.example.clinic.repository;

import com.example.clinic.entity.Patient;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    @Query("SELECT p FROM Patient p WHERE "
            + "p.firstName ILIKE CONCAT('%', :q, '%') "
            + "OR p.lastName ILIKE CONCAT('%', :q, '%') "
            + "OR TRIM(CONCAT(p.firstName, ' ', p.lastName)) ILIKE CONCAT('%', :q, '%') "
            + "OR p.email ILIKE CONCAT('%', :q, '%') "
            + "OR p.phone ILIKE CONCAT('%', :q, '%')")
    List<Patient> search(@Param("q") String q);
}
