package com.example.Sportal.service;

import com.example.Sportal.models.entities.Enrollment;
import com.example.Sportal.models.entities.User;

import java.util.List;

public interface EnrollmentService {
    
    List<Enrollment> getEnrollmentsByStudent(User student);
    
    List<Enrollment> getEnrollmentsByCourse(Long courseId);
    
    Enrollment enrollStudent(User student, Long courseId);
    
    void dropCourse(User student, Long courseId);
    
    void updateEnrollmentStatus(Long enrollmentId, Enrollment.Status status, User instructor);
    
    boolean isEnrolled(User student, Long courseId);
}
