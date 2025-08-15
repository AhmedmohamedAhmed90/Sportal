package com.example.Sportal.models.dto.material;

import com.example.Sportal.models.entities.Material;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
public class CreateMaterialRequest {
    
    @NotNull(message = "Course ID is required")
    private Long courseId;
    
    @NotBlank(message = "Material title is required")
    @Size(min = 3, max = 200, message = "Material title must be between 3 and 200 characters")
    private String title;
    
    private String description;
    
    @NotNull(message = "Visibility is required")
    private Material.Visibility visibility;
}
