package com.example.Sportal.service;

import com.example.Sportal.models.dto.course.CourseDto;
import com.example.Sportal.models.dto.course.CreateCourseRequest;
import com.example.Sportal.models.dto.course.UpdateCourseRequest;
import com.example.Sportal.models.entities.Course;
import com.example.Sportal.models.entities.User;

import java.util.List;

public interface CoursesService {
    
    List<CourseDto> getAllCourses();
    
    List<CourseDto> getCoursesByInstructor(User instructor);
    
    List<CourseDto> getPublicCourses();
    
    List<CourseDto> getVisibleCoursesForUser(User user);
    
    List<CourseDto> getEnrolledCoursesForStudent(User student);

    List<Course> getByInstructor(User instructor);

    CourseDto getCourseById(Long id);
    
    CourseDto getCourseByCode(String code);
    
    CourseDto createCourse(CreateCourseRequest request, User instructor);
    
    CourseDto updateCourse(Long id, UpdateCourseRequest request, User instructor);
    
    void deleteCourse(Long id, User instructor);
    
    boolean isInstructorOfCourse(User user, Long courseId);
    
    boolean isStudentEnrolledInCourse(User student, Long courseId);
}
