package com.example.Sportal.models.dto.assignment;


import com.example.Sportal.models.entities.Course;
import com.example.Sportal.models.entities.Submission;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class CreateAssignmentRequest {

    @NotNull(message = "Course ID is required")
    private Long courseId;

    @NotBlank(message = "title is required")
    private String title;

    @NotBlank(message = "description is required")
    private String description;

    @NotNull(message = "due Date is required")
    private LocalDateTime dueDate;

    @NotNull(message = "maxScore is required")
    private BigDecimal maxScore;

}
