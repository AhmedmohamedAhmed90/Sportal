package com.example.Sportal.service.impl;

import com.example.Sportal.models.entities.Assignment;
import com.example.Sportal.models.entities.Course;
import com.example.Sportal.repository.AssignmentsRepository;
import com.example.Sportal.service.AssignmentsService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AssignmentsServiceImpl implements AssignmentsService {

    private AssignmentsRepository assignmentsRepository;

    public AssignmentsServiceImpl(AssignmentsRepository assignmentsRepository) {
        this.assignmentsRepository = assignmentsRepository;
    }

    @Override
    public Assignment createAssignment(Assignment assignment) {
        return assignmentsRepository.save(assignment);
    }

    @Override
    public Optional<Assignment> getAssignmentById(Long id) {
        return assignmentsRepository.findById(id);
    }

    @Override
    public Page<Assignment> getAllAssignments(Pageable pageable) {
        return assignmentsRepository.findAll(pageable);
    }

    @Override
    public Page<Assignment> getAssignmentsByCourse(Long courseId, Pageable pageable) {
        return assignmentsRepository.findByCourseId(courseId, pageable);
    }

    public List<Assignment> getAssignmentsByCourses(List<Course> courses) {
        return assignmentsRepository.findAssignmentsByCourseIn(courses);
    }

    @Override
    public Assignment updateAssignment(Assignment assignment) {
        Assignment updatedAssignment = assignmentsRepository.findById(assignment.getId())
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
        if(updatedAssignment != null) {
            updatedAssignment.setTitle(assignment.getTitle());
            updatedAssignment.setDescription(assignment.getDescription());
            updatedAssignment.setCourse(assignment.getCourse());
            updatedAssignment.setDueDate(assignment.getDueDate());
            updatedAssignment.setMaxScore(assignment.getMaxScore());
        }
        return  assignmentsRepository.save(updatedAssignment);
    }

    @Override
    public void deleteAllAssignmentsByCourseId(Long courseId) {
        assignmentsRepository.deleteAllAssignmentsByCourseId(courseId);
    }

    @Override
    public void deleteAssignmentById(Long id) {
        Assignment assignment = assignmentsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        assignmentsRepository.delete(assignment);
    }



    @Override
    public Page<Assignment> searchAssignments(String searchTerm, Pageable pageable) {
        return assignmentsRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                searchTerm, searchTerm, pageable);
    }

    @Override
    public long getTotalAssignmentsCount() {
        return assignmentsRepository.count();
    }

    @Override
    public long getOverdueAssignmentsCount() {
        return assignmentsRepository.countByDueDateBefore(LocalDateTime.now());
    }

    @Override
    public long getTotalSubmissionsCount() {
        return assignmentsRepository.countTotalSubmissions();
    }

    @Override
    public long getSubmissionCountForAssignment(Long assignmentId) {
        return assignmentsRepository.countSubmissionsByAssignmentId(assignmentId);
    }
}
