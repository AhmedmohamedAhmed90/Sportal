package com.example.Sportal.repository;

import com.example.Sportal.models.entities.Course;
import com.example.Sportal.models.entities.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {
    
    List<Material> findByCourse(Course course);
    
    List<Material> findByCourseAndVisibility(Course course, Material.Visibility visibility);
    
    @Query("SELECT m FROM Material m WHERE m.course = :course AND (m.visibility = 'PUBLIC' OR :isEnrolled = true)")
    List<Material> findAccessibleMaterialsForCourse(@Param("course") Course course, @Param("isEnrolled") boolean isEnrolled);
}
