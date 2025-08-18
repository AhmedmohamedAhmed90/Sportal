package com.example.Sportal.service.impl;

import com.example.Sportal.models.dto.course.CourseDto;
import com.example.Sportal.models.dto.course.CreateCourseRequest;
import com.example.Sportal.models.dto.course.UpdateCourseRequest;
import com.example.Sportal.models.entities.Course;
import com.example.Sportal.models.entities.Enrollment;
import com.example.Sportal.models.entities.User;
import com.example.Sportal.repository.CourseRepository;
import com.example.Sportal.repository.EnrollmentRepository;
import com.example.Sportal.service.CoursesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CoursesServiceImpl implements CoursesService {

    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Override
    public List<CourseDto> getAllCourses() {
        System.out.println("=== getAllCourses Debug ===");
        List<Course> courses = courseRepository.findAll();
        System.out.println("findAll returned: " + (courses != null ? courses.size() : "null"));
        System.out.println("findAll object: " + courses);
        
        if (courses == null) {
            courses = new ArrayList<>();
        }
        
        List<CourseDto> dtos = courses.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        System.out.println("Converted to DTOs: " + dtos.size());
        System.out.println("Final result: " + dtos);
        System.out.println("=== End Debug ===");
        
        return dtos;
    }

    @Override
    public List<CourseDto> getCoursesByInstructor(User instructor) {
        System.out.println("=== getCoursesByInstructor Debug ===");
        System.out.println("Instructor: " + instructor.getName() + " (Role: " + instructor.getRole() + ")");
        
        List<Course> courses = courseRepository.findByInstructor(instructor);
        System.out.println("Repository returned: " + (courses != null ? courses.size() : "null"));
        System.out.println("Repository object: " + courses);
        
        if (courses == null) {
            System.out.println("Repository returned null, creating empty list");
            courses = new ArrayList<>();
        }
        
        List<CourseDto> dtos = courses.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        System.out.println("Converted to DTOs: " + dtos.size());
        System.out.println("Final result: " + dtos);
        System.out.println("=== End Debug ===");
        
        return dtos;
    }

    @Override
    public List<CourseDto> getPublicCourses() {
        return courseRepository.findByVisibility(Course.Visibility.PUBLIC).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CourseDto> getVisibleCoursesForUser(User user) {
        System.out.println("=== getVisibleCoursesForUser Debug ===");
        System.out.println("User: " + user.getName() + " (Role: " + user.getRole() + ")");
        
        List<Course> courses = courseRepository.findVisibleCoursesForUser(user);
        System.out.println("Repository returned: " + (courses != null ? courses.size() : "null"));
        System.out.println("Repository object: " + courses);
        
        if (courses == null) {
            System.out.println("Repository returned null, creating empty list");
            courses = new ArrayList<>();
        }
        
        List<CourseDto> dtos = courses.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        System.out.println("Converted to DTOs: " + dtos.size());
        System.out.println("Final result: " + dtos);
        System.out.println("=== End Debug ===");
        
        return dtos;
    }

    @Override
    public List<CourseDto> getEnrolledCoursesForStudent(User student) {
        List<Course> courses = courseRepository.findEnrolledCoursesForStudent(student);
        if (courses == null) {
            courses = new ArrayList<>();
        }
        return courses.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public CourseDto getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        return convertToDto(course);
    }

    @Override
    public CourseDto getCourseByCode(String code) {
        Course course = courseRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        return convertToDto(course);
    }

    @Override
    public CourseDto createCourse(CreateCourseRequest request, User instructor) {
        if (courseRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("Course code already exists");
        }
        
        Course course = new Course();
        course.setCode(request.getCode());
        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setSemester(request.getSemester());
        course.setCapacity(request.getCapacity());
        course.setVisibility(request.getVisibility());
        course.setInstructor(instructor);
        
        Course savedCourse = courseRepository.save(course);
        return convertToDto(savedCourse);
    }

    @Override
    public CourseDto updateCourse(Long id, UpdateCourseRequest request, User instructor) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        if (!course.getInstructor().getId().equals(instructor.getId())) {
            throw new RuntimeException("Only the instructor can update this course");
        }
        
        if (request.getCode() != null && !request.getCode().equals(course.getCode())) {
            if (courseRepository.existsByCode(request.getCode())) {
                throw new RuntimeException("Course code already exists");
            }
            course.setCode(request.getCode());
        }
        
        if (request.getTitle() != null) {
            course.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            course.setDescription(request.getDescription());
        }
        if (request.getSemester() != null) {
            course.setSemester(request.getSemester());
        }
        if (request.getCapacity() != null) {
            course.setCapacity(request.getCapacity());
        }
        if (request.getVisibility() != null) {
            course.setVisibility(request.getVisibility());
        }
        
        Course updatedCourse = courseRepository.save(course);
        return convertToDto(updatedCourse);
    }

    @Override
    public void deleteCourse(Long id, User instructor) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        if (!course.getInstructor().getId().equals(instructor.getId())) {
            throw new RuntimeException("Only the instructor can delete this course");
        }
        
        courseRepository.delete(course);
    }

    @Override
    public boolean isInstructorOfCourse(User user, Long courseId) {
        return courseRepository.findById(courseId)
                .map(course -> course.getInstructor().getId().equals(user.getId()))
                .orElse(false);
    }

    @Override
    public boolean isStudentEnrolledInCourse(User student, Long courseId) {
        return enrollmentRepository.findByStudentAndCourse(student, 
                courseRepository.findById(courseId).orElse(null))
                .map(enrollment -> enrollment.getStatus() == Enrollment.Status.ENROLLED)
                .orElse(false);
    }
    
    private CourseDto convertToDto(Course course) {
        CourseDto dto = new CourseDto();
        dto.setId(course.getId());
        dto.setCode(course.getCode());
        dto.setTitle(course.getTitle());
        dto.setDescription(course.getDescription());
        dto.setInstructorId(course.getInstructor().getId());
        dto.setInstructorName(course.getInstructor().getName());
        dto.setSemester(course.getSemester());
        dto.setCapacity(course.getCapacity());
        dto.setVisibility(course.getVisibility());
        dto.setCreatedAt(course.getCreatedAt());
        
        
        long enrolledCount = 0;
        if (course.getEnrollments() != null) {
            enrolledCount = course.getEnrollments().stream()
                    .filter(e -> e != null && e.getStatus() == Enrollment.Status.ENROLLED)
                    .count();
        }
        dto.setEnrolledStudents((int) enrolledCount);
        
        return dto;
    }
}
