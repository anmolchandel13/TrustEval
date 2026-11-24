package com.anmol.trusteval.repository;

import com.anmol.trusteval.entity.EvaluationTask;
import com.anmol.trusteval.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvaluationTaskRepository extends JpaRepository<EvaluationTask, Long> {
    List<EvaluationTask> findByStatus(TaskStatus status);
}
