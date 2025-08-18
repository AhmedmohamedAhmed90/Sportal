package com.example.Sportal.service;

import com.example.Sportal.models.entities.Assignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface AssignmentsService {

    Assignment createAssignment(Assignment assignment);

    Page<Assignment> getAllAssignments(Pageable pageable);
    Page<Assignment> searchAssignments(String searchTerm, Pageable pageable);
    Page<Assignment> getAssignmentsByCourse(Long courseId, Pageable pageable);

    Optional<Assignment> getAssignmentById(Long id);

    Assignment updateAssignment(Assignment assignment);

    void deleteAllAssignmentsByCourseId(Long courseId);
    void deleteAssignmentById(Long id);

    long getTotalAssignmentsCount();
    long getOverdueAssignmentsCount();
    long getTotalSubmissionsCount();
    long getSubmissionCountForAssignment(Long assignmentId);
}
