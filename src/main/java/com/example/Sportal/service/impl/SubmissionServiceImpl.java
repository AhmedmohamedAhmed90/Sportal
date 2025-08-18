package com.example.Sportal.service.impl;

import com.example.Sportal.models.entities.Assignment;
import com.example.Sportal.models.entities.Submission;
import com.example.Sportal.models.entities.User;
import com.example.Sportal.repository.SubmissionRepository;
import com.example.Sportal.repository.AssignmentsRepository;
import com.example.Sportal.repository.UserRepository;
import com.example.Sportal.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final AssignmentsRepository assignmentRepository;
    private final UserRepository userRepository;

    @Value("${app.upload.dir:uploads/submissions}")
    private String uploadDir;

    @Value("${app.upload.max-size:10485760}") // 10MB default
    private long maxFileSize;

    @Override
    public Submission submitAssignment(Long assignmentId, Long studentId,
                                       MultipartFile file, String comments) throws IOException {

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        validateFile(file);
        if (!isStudentEnrolledInCourse(student, assignment.getCourse().getId())) {
            throw new IllegalArgumentException("Student is not enrolled in this course");
        }

        String filePath = saveFile(file, assignment, student);
        Submission.Status status = determineSubmissionStatus(assignment);
        Submission submission = findExistingSubmission(assignment, student)
                .map(existing -> updateExistingSubmission(existing, filePath, status, comments))
                .orElse(createNewSubmission(assignment, student, filePath, status, comments));

        log.info("Assignment submitted: Assignment ID = {}, Student ID = {}, Status = {}",
                assignmentId, studentId, status);

        return submissionRepository.save(submission);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Submission> getSubmissionsByAssignment(Long assignmentId) {
        return submissionRepository.findByAssignmentIdOrderBySubmittedAtDesc(assignmentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Submission> getSubmissionsByStudent(Long studentId) {
        return submissionRepository.findByStudentIdOrderBySubmittedAtDesc(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Submission> getStudentSubmissionsForAssignment(Long assignmentId, Long studentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        return submissionRepository.findByAssignmentAndStudentOrderBySubmittedAtDesc(assignment, student);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Submission> getSubmissionById(Long id) {
        return submissionRepository.findById(id);
    }

    @Override
    public List<Submission> getSubmissionByAssignments(List<Assignment> assignments){
        return submissionRepository.findSubmissionsByAssignmentIn(assignments);
    }

    @Override
    public Submission gradeSubmission(Long submissionId, BigDecimal score, String feedback) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found"));

        if (score.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Score cannot be negative");
        }

        BigDecimal maxScore = submission.getAssignment().getMaxScore();
        if (maxScore != null && score.compareTo(maxScore) > 0) {
            throw new IllegalArgumentException("Score must be between 0 and " + maxScore);
        }

        submission.setScore(score);
        submission.setFeedback(feedback);

        log.info("Submission graded: ID = {}, Score = {}", submissionId, score);

        return submissionRepository.save(submission);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasStudentSubmitted(Long assignmentId, Long studentId) {
        return submissionRepository.existsByAssignmentIdAndStudentId(assignmentId, studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getSubmissionCount(Long assignmentId) {
        return submissionRepository.countByAssignmentId(assignmentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Submission> getUngraduatedSubmissions(Long assignmentId) {
        return submissionRepository.findUngraduatedSubmissionsByAssignment(assignmentId);
    }

    @Override
    public void deleteSubmission(Long submissionId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found"));

        try {
            Path filePath = Paths.get(submission.getFilePath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("Failed to delete file: {}", submission.getFilePath(), e);
        }

        submissionRepository.delete(submission);
        log.info("Submission deleted: ID = {}", submissionId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isSubmissionOwner(Long submissionId, Long userId) {
        return submissionRepository.findById(submissionId)
                .map(submission -> submission.getStudent().getId().equals(userId))
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Submission> getSubmissionsByCourseAndStudent(Long courseId, Long studentId) {
        return submissionRepository.findByCourseAndStudentId(courseId, studentId);
    }


    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size");
        }

        String contentType = file.getContentType();
        if (contentType == null || !isAllowedFileType(contentType)) {
            throw new IllegalArgumentException("File type not allowed");
        }
    }

    private boolean isAllowedFileType(String contentType) {
        return contentType.equals("application/pdf") ||
                contentType.equals("application/msword") ||
                contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
                contentType.equals("text/plain") ||
                contentType.equals("application/zip");
    }

    private boolean isStudentEnrolledInCourse(User student, Long courseId) {
        return student.getEnrollments().stream()
                .anyMatch(enrollment -> enrollment.getCourse().getId().equals(courseId));
    }

    private String saveFile(MultipartFile file, Assignment assignment, User student) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";

        String uniqueFilename = String.format("%s_%s_%s_%s%s",
                assignment.getId(),
                student.getId(),
                UUID.randomUUID().toString().substring(0, 8),
                System.currentTimeMillis(),
                extension);

        Path filePath = uploadPath.resolve(uniqueFilename);

        // Save file
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filePath.toString();
    }

    private Submission.Status determineSubmissionStatus(Assignment assignment) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dueDate = assignment.getDueDate();

        if (now.isAfter(dueDate)) {
            return Submission.Status.LATE;
        }

        return Submission.Status.SUBMITTED;
    }

    private Optional<Submission> findExistingSubmission(Assignment assignment, User student) {
        return submissionRepository.findByAssignmentIdAndStudentId(assignment.getId(), student.getId());
    }

    private Submission updateExistingSubmission(Submission existing, String filePath,
                                                Submission.Status status, String comments) {
        // Delete old file
        try {
            Path oldFilePath = Paths.get(existing.getFilePath());
            Files.deleteIfExists(oldFilePath);
        } catch (IOException e) {
            log.warn("Failed to delete old file: {}", existing.getFilePath(), e);
        }

        existing.setFilePath(filePath);
        existing.setStatus(Submission.Status.RESUBMITTED);
        existing.setSubmittedAt(LocalDateTime.now());
        existing.setScore(null);
        existing.setFeedback(comments);

        return existing;
    }

    private Submission createNewSubmission(Assignment assignment, User student, String filePath,
                                           Submission.Status status, String comments) {
        Submission submission = new Submission();
        submission.setAssignment(assignment);
        submission.setStudent(student);
        submission.setFilePath(filePath);
        submission.setStatus(status);
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setFeedback(comments);

        return submission;
    }
}