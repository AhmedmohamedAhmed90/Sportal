package com.example.Sportal.models.dto.assignment;

import com.example.Sportal.models.entities.Course;
import com.example.Sportal.models.entities.Submission;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class AssignmentDto {
    private Long id;
    private Course course;
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private BigDecimal maxScore;

}
