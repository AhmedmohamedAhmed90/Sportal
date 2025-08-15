package com.example.Sportal.models.dto.material;

import com.example.Sportal.models.entities.Material;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MaterialDto {
    private Long id;
    private Long courseId;
    private String courseTitle;
    private String title;
    private String description;
    private String filePath;
    private String fileType;
    private String fileName;
    private Long fileSize;
    private Material.Visibility visibility;
    private LocalDateTime uploadedAt;
}
