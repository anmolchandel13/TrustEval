package com.anmol.trusteval.repository;

import com.anmol.trusteval.entity.TaskAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, Long> {
    boolean existsByEvaluatorIdAndTaskId(Long evaluatorId, Long taskId);
    Optional<TaskAssignment> findByEvaluatorIdAndTaskId(Long evaluatorId, Long taskId);
    List<TaskAssignment> findByEvaluatorId(Long evaluatorId);
}
