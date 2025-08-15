package com.example.Sportal.models.dto.course;

import com.example.Sportal.models.entities.Course;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CourseDto {
    private Long id;
    private String code;
    private String title;
    private String description;
    private Long instructorId;
    private String instructorName;
    private String semester;
    private Integer capacity;
    private Course.Visibility visibility;
    private LocalDateTime createdAt;
    private Integer enrolledStudents;
    private boolean enrolled;
}
