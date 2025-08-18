package com.example.Sportal.repository;
import com.example.Sportal.models.entities.Submission;
import com.example.Sportal.models.entities.Assignment;
import com.example.Sportal.models.entities.User;
import org.modelmapper.internal.bytebuddy.dynamic.scaffold.subclass.SubclassDynamicTypeBuilder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    List<Submission> findByAssignmentIdOrderBySubmittedAtDesc(Long assignmentId);

    List<Submission> findByStudentIdOrderBySubmittedAtDesc(Long studentId);

    List<Submission> findByAssignmentAndStudentOrderBySubmittedAtDesc(Assignment assignment, User student);

    List<Submission> findSubmissionsByAssignmentIn(List<Assignment> assignments);

    Optional<Submission> findByAssignmentIdAndStudentId(Long assignmentId, Long studentId);

    @Query("SELECT s FROM Submission s WHERE s.assignment.course.id = :courseId AND s.student.id = :studentId")
    List<Submission> findByCourseAndStudentId(@Param("courseId") Long courseId, @Param("studentId") Long studentId);

    @Query("SELECT s FROM Submission s WHERE s.assignment.id = :assignmentId AND s.score IS NULL")
    List<Submission> findUngraduatedSubmissionsByAssignment(@Param("assignmentId") Long assignmentId);

    boolean existsByAssignmentIdAndStudentId(Long assignmentId, Long studentId);

    long countByAssignmentId(Long assignmentId);
}