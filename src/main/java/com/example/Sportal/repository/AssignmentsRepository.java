package com.example.Sportal.repository;

import com.example.Sportal.models.entities.Assignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentsRepository extends JpaRepository<Assignment, Long> {
    Page<Assignment> findByCourseId(Long courseId, Pageable pageable);

    Page<Assignment> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String title, String description, Pageable pageable);

    void deleteAllAssignmentsByCourseId(long courseId);

    long countByDueDateAfter(LocalDateTime dateTime);
    long countByDueDateBefore(LocalDateTime dateTime);

    @Query("SELECT COUNT(s) FROM Submission s WHERE s.assignment.id IN (SELECT a.id FROM Assignment a)")
    long countTotalSubmissions();

    @Query("SELECT COUNT(s) FROM Submission s WHERE s.assignment.id = :assignmentId")
    long countSubmissionsByAssignmentId(@Param("assignmentId") Long assignmentId);

}
