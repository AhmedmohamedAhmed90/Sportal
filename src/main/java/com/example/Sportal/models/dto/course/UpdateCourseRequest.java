package com.example.Sportal.models.dto.course;

import com.example.Sportal.models.entities.Course;
import lombok.Data;

import jakarta.validation.constraints.Size;

@Data
public class UpdateCourseRequest {
    
    @Size(min = 3, max = 50, message = "Course code must be between 3 and 50 characters")
    private String code;
    
    @Size(min = 3, max = 200, message = "Course title must be between 3 and 200 characters")
    private String title;
    
    private String description;
    
    @Size(max = 20, message = "Semester must not exceed 20 characters")
    private String semester;
    
    private Integer capacity;
    
    private Course.Visibility visibility;
}
