package com.example.Sportal.service.impl;

import com.example.Sportal.models.entities.Course;
import com.example.Sportal.models.entities.Enrollment;
import com.example.Sportal.models.entities.User;
import com.example.Sportal.repository.CourseRepository;
import com.example.Sportal.repository.EnrollmentRepository;
import com.example.Sportal.service.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EnrollmentServiceImpl implements EnrollmentService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;
    
    @Autowired
    private CourseRepository courseRepository;

    @Override
    public List<Enrollment> getEnrollmentsByStudent(User student) {
        return enrollmentRepository.findByStudent(student);
    }

    @Override
    public List<Enrollment> getEnrollmentsByCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        return enrollmentRepository.findByCourse(course);
    }

    @Override
    public Enrollment enrollStudent(User student, Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        // Check if already enrolled
        if (enrollmentRepository.existsByStudentAndCourse(student, course)) {
            throw new RuntimeException("Student is already enrolled in this course");
        }
        
        // Check course capacity
        if (course.getCapacity() != null) {
            long enrolledCount = course.getEnrollments().stream()
                    .filter(e -> e.getStatus() == Enrollment.Status.ENROLLED)
                    .count();
            if (enrolledCount >= course.getCapacity()) {
                throw new RuntimeException("Course is at full capacity");
            }
        }
        
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setStatus(Enrollment.Status.ENROLLED);
        
        return enrollmentRepository.save(enrollment);
    }

    @Override
    public void dropCourse(User student, Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        Enrollment enrollment = enrollmentRepository.findByStudentAndCourse(student, course)
                .orElseThrow(() -> new RuntimeException("Student is not enrolled in this course"));
        
        enrollmentRepository.delete(enrollment);
    }

    @Override
    public void updateEnrollmentStatus(Long enrollmentId, Enrollment.Status status, User instructor) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));
        
        if (!enrollment.getCourse().getInstructor().getId().equals(instructor.getId())) {
            throw new RuntimeException("Only the course instructor can update enrollment status");
        }
        
        enrollment.setStatus(status);
        enrollmentRepository.save(enrollment);
    }

    @Override
    public boolean isEnrolled(User student, Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        return enrollmentRepository.findByStudentAndCourse(student, course)
                .map(enrollment -> enrollment.getStatus() == Enrollment.Status.ENROLLED)
                .orElse(false);
    }
}
