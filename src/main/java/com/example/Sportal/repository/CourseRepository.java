package com.example.Sportal.repository;

import com.example.Sportal.models.entities.Course;
import com.example.Sportal.models.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    
    List<Course> findByInstructor(User instructor);
    
    List<Course> findByVisibility(Course.Visibility visibility);
    
    Optional<Course> findByCode(String code);
    
    boolean existsByCode(String code);
    
    @Query("SELECT c FROM Course c WHERE c.visibility = 'PUBLIC' OR c.instructor = :user")
    List<Course> findVisibleCoursesForUser(@Param("user") User user);
    
    @Query("SELECT c FROM Course c JOIN c.enrollments e WHERE e.student = :student AND e.status = 'ENROLLED'")
    List<Course> findEnrolledCoursesForStudent(@Param("student") User student);
}
