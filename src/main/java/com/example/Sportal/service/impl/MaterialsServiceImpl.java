package com.example.Sportal.service.impl;

import com.example.Sportal.models.dto.material.MaterialDto;
import com.example.Sportal.models.dto.material.CreateMaterialRequest;
import com.example.Sportal.models.entities.Course;
import com.example.Sportal.models.entities.Material;
import com.example.Sportal.models.entities.User;
import com.example.Sportal.repository.CourseRepository;
import com.example.Sportal.repository.MaterialRepository;
import com.example.Sportal.service.CoursesService;
import com.example.Sportal.service.MaterialsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MaterialsServiceImpl implements MaterialsService {

    @Autowired
    private MaterialRepository materialRepository;
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private CoursesService coursesService;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Override
    public List<MaterialDto> getMaterialsByCourse(Long courseId, User user) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        boolean isEnrolled = coursesService.isStudentEnrolledInCourse(user, courseId);
        boolean isInstructor = coursesService.isInstructorOfCourse(user, courseId);
        
        List<Material> materials;
        if (isInstructor) {
            materials = materialRepository.findByCourse(course);
        } else {
            materials = materialRepository.findAccessibleMaterialsForCourse(course, isEnrolled);
        }
        
        return materials.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public MaterialDto getMaterialById(Long id, User user) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Material not found"));
        
        if (!canAccessMaterial(user, id)) {
            throw new RuntimeException("Access denied to this material");
        }
        
        return convertToDto(material);
    }

    @Override
    public MaterialDto uploadMaterial(CreateMaterialRequest request, MultipartFile file, User instructor) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        if (!coursesService.isInstructorOfCourse(instructor, request.getCourseId())) {
            throw new RuntimeException("Only the instructor can upload materials to this course");
        }
        
        if (file.isEmpty()) {
            throw new RuntimeException("File cannot be empty");
        }
        
        // Validate file type
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = getFileExtension(originalFilename);
        if (!isValidFileType(fileExtension)) {
            throw new RuntimeException("Invalid file type. Allowed types: PDF, DOC, DOCX, PPT, PPTX, JPG, PNG");
        }
        
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            try {
                Files.createDirectories(uploadPath);
            } catch (IOException e) {
                throw new RuntimeException("Could not create upload directory", e);
            }
        }
        
        // Generate unique filename
        String fileName = UUID.randomUUID().toString() + "_" + originalFilename;
        Path filePath = uploadPath.resolve(fileName);
        
        try {
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Could not store file", e);
        }
        
        Material material = new Material();
        material.setCourse(course);
        material.setTitle(request.getTitle());
        material.setDescription(request.getDescription());
        material.setFilePath(filePath.toString());
        material.setFileType(fileExtension);
        material.setVisibility(request.getVisibility());
        
        Material savedMaterial = materialRepository.save(material);
        return convertToDto(savedMaterial);
    }

    @Override
    public void deleteMaterial(Long id, User instructor) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Material not found"));
        
        if (!coursesService.isInstructorOfCourse(instructor, material.getCourse().getId())) {
            throw new RuntimeException("Only the instructor can delete materials from this course");
        }
        
        // Delete file from filesystem
        try {
            Path filePath = Paths.get(material.getFilePath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Log error but continue with database deletion
            System.err.println("Could not delete file: " + material.getFilePath());
        }
        
        materialRepository.delete(material);
    }

    @Override
    public Resource downloadMaterial(Long id, User user) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Material not found"));
        
        if (!canAccessMaterial(user, id)) {
            throw new RuntimeException("Access denied to this material");
        }
        
        try {
            Path filePath = Paths.get(material.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("File not found or not readable");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("File not found", e);
        }
    }

    @Override
    public boolean canAccessMaterial(User user, Long materialId) {
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("Material not found"));
        
        // Instructor can access all materials in their course
        if (coursesService.isInstructorOfCourse(user, material.getCourse().getId())) {
            return true;
        }
        
        // Public materials are accessible to everyone
        if (material.getVisibility() == Material.Visibility.PUBLIC) {
            return true;
        }
        
        // Enrolled students can access ENROLLED materials
        if (material.getVisibility() == Material.Visibility.ENROLLED) {
            return coursesService.isStudentEnrolledInCourse(user, material.getCourse().getId());
        }
        
        return false;
    }
    
    private MaterialDto convertToDto(Material material) {
        MaterialDto dto = new MaterialDto();
        dto.setId(material.getId());
        dto.setCourseId(material.getCourse().getId());
        dto.setCourseTitle(material.getCourse().getTitle());
        dto.setTitle(material.getTitle());
        dto.setDescription(material.getDescription());
        dto.setFilePath(material.getFilePath());
        dto.setFileType(material.getFileType());
        dto.setVisibility(material.getVisibility());
        dto.setUploadedAt(material.getUploadedAt());
        
        // Extract filename from filepath
        if (material.getFilePath() != null) {
            String fileName = Paths.get(material.getFilePath()).getFileName().toString();
            // Remove UUID prefix
            if (fileName.contains("_")) {
                fileName = fileName.substring(fileName.indexOf("_") + 1);
            }
            dto.setFileName(fileName);
        }
        
        // Get file size
        try {
            Path filePath = Paths.get(material.getFilePath());
            if (Files.exists(filePath)) {
                dto.setFileSize(Files.size(filePath));
            }
        } catch (IOException e) {
            dto.setFileSize(0L);
        }
        
        return dto;
    }
    
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }
    
    private boolean isValidFileType(String extension) {
        String[] allowedExtensions = {"pdf", "doc", "docx", "ppt", "pptx", "jpg", "jpeg", "png"};
        for (String allowed : allowedExtensions) {
            if (allowed.equals(extension)) {
                return true;
            }
        }
        return false;
    }
}
