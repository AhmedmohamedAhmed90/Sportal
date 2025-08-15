package com.example.Sportal.service;

import com.example.Sportal.models.dto.material.MaterialDto;
import com.example.Sportal.models.dto.material.CreateMaterialRequest;
import com.example.Sportal.models.entities.User;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MaterialsService {
    
    List<MaterialDto> getMaterialsByCourse(Long courseId, User user);
    
    MaterialDto getMaterialById(Long id, User user);
    
    MaterialDto uploadMaterial(CreateMaterialRequest request, MultipartFile file, User instructor);
    
    void deleteMaterial(Long id, User instructor);
    
    Resource downloadMaterial(Long id, User user);
    
    boolean canAccessMaterial(User user, Long materialId);
}
