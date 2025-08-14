package com.example.Sportal.controller;

import com.example.Sportal.models.entities.Course;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(path = "/courses")
public class CoursesController {

    //Inject Repository

    @GetMapping(path = "/")
    public List<Course> getCourses() {
        List<Course> courses = new ArrayList<>();
        return courses;
    }


    @GetMapping(path="/{id}")
    public Long getCourse(@PathVariable("id") Long id) {
        return id;
    }

    @PostMapping()
    public void createCourse(@RequestBody Course course) {

    }

    @PutMapping(path = "/{id}")
    public void updateCourse(@RequestBody Course course){
        //Logic of update course;
    }

    @DeleteMapping()
    public String deleteAllCourses(){
        // logic of deleted from DB;

        return "All Courses deleted";
    }

}
