package com.example.Sportal.repository;

import com.example.Sportal.models.entities.Course;
import com.example.Sportal.models.entities.Enrollment;
import com.example.Sportal.models.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    
    List<Enrollment> findByStudent(User student);
    
    List<Enrollment> findByCourse(Course course);
    
    Optional<Enrollment> findByStudentAndCourse(User student, Course course);
    
    boolean existsByStudentAndCourse(User student, Course course);
    
    List<Enrollment> findByStudentAndStatus(User student, Enrollment.Status status);
}
