package com.anmol.trusteval.repository;

import com.anmol.trusteval.entity.Evaluator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EvaluatorRepository extends JpaRepository<Evaluator, Long> {
    Optional<Evaluator> findByEmail(String email);
}
