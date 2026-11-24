package com.anmol.trusteval.repository;

import com.anmol.trusteval.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    boolean existsByEvaluatorIdAndTaskId(Long evaluatorId, Long taskId);
    List<Submission> findByTaskId(Long taskId);
}
