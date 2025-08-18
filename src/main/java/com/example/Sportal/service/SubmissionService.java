package com.example.Sportal.service;

import com.example.Sportal.models.entities.Assignment;
import com.example.Sportal.models.entities.Submission;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface SubmissionService {

    Submission submitAssignment(Long assignmentId, Long studentId,
                                MultipartFile file, String comments) throws IOException;


    List<Submission> getSubmissionsByAssignment(Long assignmentId);

    List<Submission> getSubmissionsByStudent(Long studentId);

    List<Submission> getStudentSubmissionsForAssignment(Long assignmentId, Long studentId);

    Optional<Submission> getSubmissionById(Long id);

    List<Submission> getSubmissionByAssignments(List<Assignment> assignments);

    Submission gradeSubmission(Long submissionId, BigDecimal score, String feedback);

    boolean hasStudentSubmitted(Long assignmentId, Long studentId);

    long getSubmissionCount(Long assignmentId);

    List<Submission> getUngraduatedSubmissions(Long assignmentId);

    void deleteSubmission(Long submissionId);

    boolean isSubmissionOwner(Long submissionId, Long userId);

    List<Submission> getSubmissionsByCourseAndStudent(Long courseId, Long studentId);
}
