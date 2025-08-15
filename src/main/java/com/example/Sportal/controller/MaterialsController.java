package com.example.Sportal.controller;

import com.example.Sportal.models.dto.material.MaterialDto;
import com.example.Sportal.models.dto.material.CreateMaterialRequest;
import com.example.Sportal.models.entities.User;
import com.example.Sportal.security.CustomUserDetails;
import com.example.Sportal.service.MaterialsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.ArrayList;

@Controller
@RequestMapping("/materials")
public class MaterialsController {

    @Autowired
    private MaterialsService materialsService;

    @GetMapping("/course/{courseId}")
    public String listMaterialsByCourse(@PathVariable Long courseId, Model model) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            model.addAttribute("user", currentUser);
            model.addAttribute("courseId", courseId);
            
            List<MaterialDto> materials = new ArrayList<>();
            try {
                materials = materialsService.getMaterialsByCourse(courseId, currentUser);
            } catch (Exception e) {
                System.err.println("Error fetching materials: " + e.getMessage());
                e.printStackTrace();
            }
            
            model.addAttribute("materials", materials);
            return "materials/list";
            
        } catch (Exception e) {
            System.err.println("Error in listMaterialsByCourse: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Unable to load materials. Please try again.");
            model.addAttribute("materials", new ArrayList<>());
            return "materials/list";
        }
    }

    @GetMapping("/upload/{courseId}")
    public String uploadMaterialForm(@PathVariable Long courseId, Model model) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            if (currentUser.getRole() != User.Role.INSTRUCTOR) {
                return "redirect:/courses/" + courseId;
            }
            
            model.addAttribute("user", currentUser);
            model.addAttribute("courseId", courseId);
            model.addAttribute("createMaterialRequest", new CreateMaterialRequest());
            return "materials/upload";
            
        } catch (Exception e) {
            System.err.println("Error in uploadMaterialForm: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/courses/" + courseId;
        }
    }

    @PostMapping("/upload/{courseId}")
    public String uploadMaterial(
            @PathVariable Long courseId,
            @RequestParam("file") MultipartFile file,
            @Valid @ModelAttribute CreateMaterialRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            if (currentUser.getRole() != User.Role.INSTRUCTOR) {
                redirectAttributes.addFlashAttribute("error", "Only instructors can upload materials.");
                return "redirect:/courses/" + courseId;
            }
            
            if (bindingResult.hasErrors()) {
                model.addAttribute("user", currentUser);
                model.addAttribute("courseId", courseId);
                return "materials/upload";
            }
            
            if (file.isEmpty()) {
                model.addAttribute("user", currentUser);
                model.addAttribute("courseId", courseId);
                model.addAttribute("error", "Please select a file to upload.");
                return "materials/upload";
            }
            
            MaterialDto material = materialsService.uploadMaterial(request, file, currentUser);
            redirectAttributes.addFlashAttribute("success", "Material uploaded successfully!");
            return "redirect:/courses/" + courseId;
            
        } catch (Exception e) {
            System.err.println("Error uploading material: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("user", getCurrentUser());
            model.addAttribute("courseId", courseId);
            model.addAttribute("error", e.getMessage());
            return "materials/upload";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteMaterial(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            if (currentUser.getRole() != User.Role.INSTRUCTOR) {
                redirectAttributes.addFlashAttribute("error", "Only instructors can delete materials.");
                return "redirect:/courses";
            }
            
            materialsService.deleteMaterial(id, currentUser);
            redirectAttributes.addFlashAttribute("success", "Material deleted successfully!");
            return "redirect:/courses";
            
        } catch (Exception e) {
            System.err.println("Error deleting material: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/courses";
        }
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadMaterial(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(401).build();
            }
            
            Resource resource = materialsService.downloadMaterial(id, currentUser);
            
            // Get the original filename
            String filename = "material";
            try {
                String resourceFilename = resource.getFilename();
                if (resourceFilename != null && resourceFilename.contains("_")) {
                    filename = resourceFilename.substring(resourceFilename.indexOf("_") + 1);
                }
            } catch (Exception e) {
                // Use default filename if extraction fails
            }
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(resource);
                    
        } catch (Exception e) {
            System.err.println("Error downloading material: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    private User getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && 
                authentication.getPrincipal() instanceof CustomUserDetails && 
                !"anonymousUser".equals(authentication.getPrincipal()) &&
                authentication.isAuthenticated()) {
                
                return ((CustomUserDetails) authentication.getPrincipal()).getUser();
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error getting current user: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
