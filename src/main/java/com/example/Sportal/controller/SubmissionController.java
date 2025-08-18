package com.example.Sportal.controller;


import com.example.Sportal.models.entities.Assignment;
import com.example.Sportal.models.entities.Submission;
import com.example.Sportal.models.entities.User;
import com.example.Sportal.security.CustomUserDetails;
import com.example.Sportal.service.AssignmentsService;
import com.example.Sportal.service.SubmissionService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
@RequestMapping("/submission")
@RequiredArgsConstructor
@Slf4j
public class SubmissionController {

    private final SubmissionService submissionService;
    private final AssignmentsService assignmentService;

    @GetMapping("/{assignmentId}/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public String showSubmissionForm(@PathVariable Long assignmentId, Model model, HttpSession session) {
        User currentUser = getCurrentUser();

        Assignment assignment = assignmentService.getAssignmentById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));

        List<Submission> submissions = submissionService
                .getStudentSubmissionsForAssignment(assignmentId, currentUser.getId());

        model.addAttribute("assignment", assignment);
        model.addAttribute("submissions", submissions);
        model.addAttribute("user", currentUser);
        model.addAttribute("hasSubmitted", !submissions.isEmpty());

        return "submissions/submit-form";
    }

    @PostMapping("/{assignmentId}/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public String submitAssignment(@PathVariable Long assignmentId,
                                   @RequestParam("file") MultipartFile file,
                                   @RequestParam(value = "comments", required = false) String comments,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {

        try {
            User currentUser = getCurrentUser();

            Submission submission = submissionService.submitAssignment(
                    assignmentId, currentUser.getId(), file, comments);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Assignment submitted successfully!");
            redirectAttributes.addFlashAttribute("submissionId", submission.getId());

            return "redirect:/submission/" + assignmentId + "/submit";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/submission/" + assignmentId + "/submit";

        } catch (IOException e) {
            log.error("File upload failed for assignment {}", assignmentId, e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "File upload failed. Please try again.");
            return "redirect:/submission/" + assignmentId + "/submit";
        }
    }

    @GetMapping("/{assignmentId}/submissions")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public String viewSubmissions(@PathVariable Long assignmentId, Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");

        Assignment assignment = assignmentService.getAssignmentById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));

        if (!assignment.getCourse().getInstructor().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Access denied");
        }

        List<Submission> submissions = submissionService.getSubmissionsByAssignment(assignmentId);
        long submissionCount = submissionService.getSubmissionCount(assignmentId);
        long ungraduatedCount = submissionService.getUngraduatedSubmissions(assignmentId).size();

        model.addAttribute("assignment", assignment);
        model.addAttribute("submissions", submissions);
        model.addAttribute("submissionCount", submissionCount);
        model.addAttribute("ungraduatedCount", ungraduatedCount);
        model.addAttribute("user", currentUser);

        return "submissions/list";
    }

    @GetMapping("/{submissionId}")
    @PreAuthorize("hasRole('INSTRUCTOR') or (hasRole('STUDENT') and @submissionService.isSubmissionOwner(#submissionId, authentication.principal.id))")
    public String viewSubmission(@PathVariable Long submissionId, Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");

        Submission submission = submissionService.getSubmissionById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found"));

        model.addAttribute("submission", submission);
        model.addAttribute("user", currentUser);
        model.addAttribute("isInstructor", currentUser.getRole().name().equals("INSTRUCTOR"));

        return "submissions/view";
    }

    @PostMapping("/{submissionId}/grade")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public String gradeSubmission(@PathVariable Long submissionId,
                                  @RequestParam BigDecimal score,
                                  @RequestParam(required = false) String feedback,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {

        try {
            User currentUser = (User) session.getAttribute("user");
            Submission submission = submissionService.getSubmissionById(submissionId)
                    .orElseThrow(() -> new IllegalArgumentException("Submission not found"));

            if (!submission.getAssignment().getCourse().getInstructor().getId().equals(currentUser.getId())) {
                throw new IllegalArgumentException("Access denied");
            }

            submissionService.gradeSubmission(submissionId, score, feedback);

            redirectAttributes.addFlashAttribute("successMessage", "Submission graded successfully!");

            return "redirect:/submission/" + submission.getAssignment().getId() + "/submissions";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/submissions/" + submissionId;
        }
    }

    @GetMapping("/{submissionId}/download")
    @PreAuthorize("hasRole('INSTRUCTOR') or (hasRole('STUDENT') and @submissionService.isSubmissionOwner(#submissionId, authentication.principal.id))")
    public ResponseEntity<Resource> downloadSubmission(@PathVariable Long submissionId) {

        try {
            Submission submission = submissionService.getSubmissionById(submissionId)
                    .orElseThrow(() -> new IllegalArgumentException("Submission not found"));

            Path filePath = Paths.get(submission.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new RuntimeException("File not found or not readable");
            }

            String originalFilename = submission.getStudent().getName() + "_" +
                    submission.getAssignment().getTitle() + "_submission";

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + originalFilename + "\"")
                    .body(resource);

        } catch (Exception e) {
            log.error("Error downloading submission {}", submissionId, e);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{submissionId}")
    @PreAuthorize("hasRole('INSTRUCTOR') or (hasRole('STUDENT') and @submissionService.isSubmissionOwner(#submissionId, authentication.principal.id))")
    public ResponseEntity<String> deleteSubmission(@PathVariable Long submissionId, HttpSession session) {

        try {
            User currentUser = (User) session.getAttribute("user");
            Submission submission = submissionService.getSubmissionById(submissionId)
                    .orElseThrow(() -> new IllegalArgumentException("Submission not found"));

            boolean canDelete = submission.getStudent().getId().equals(currentUser.getId()) ||
                    (currentUser.getRole().name().equals("INSTRUCTOR") &&
                            submission.getAssignment().getCourse().getInstructor().getId().equals(currentUser.getId()));

            if (!canDelete) {
                return ResponseEntity.status(403).body("Access denied");
            }

            submissionService.deleteSubmission(submissionId);

            return ResponseEntity.ok("Submission deleted successfully");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting submission {}", submissionId, e);
            return ResponseEntity.status(500).body("An error occurred while deleting the submission");
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(IllegalArgumentException e,
                                                 RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        return "redirect:/dashboard";
    }

    private User getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("=== Dashboard getCurrentUser Debug ===");
            System.out.println("Authentication: " + authentication);

            if (authentication != null) {
                System.out.println("Principal: " + authentication.getPrincipal());
                System.out.println("Principal type: " + authentication.getPrincipal().getClass().getName());
                System.out.println("Is authenticated: " + authentication.isAuthenticated());
                System.out.println("Is anonymous: " + "anonymousUser".equals(authentication.getPrincipal()));
            }

            if (authentication != null &&
                    authentication.getPrincipal() instanceof CustomUserDetails &&
                    !"anonymousUser".equals(authentication.getPrincipal()) &&
                    authentication.isAuthenticated()) {

                User user = ((CustomUserDetails) authentication.getPrincipal()).getUser();
                System.out.println("User retrieved: " + user.getName() + " (Role: " + user.getRole() + ")");
                return user;
            }

            System.out.println("No valid authentication found - returning null");
            return null;
        } catch (Exception e) {
            System.err.println("Error getting current user: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}