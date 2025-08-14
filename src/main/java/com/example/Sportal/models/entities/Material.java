package com.example.Sportal.models.entities;


import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "materials")
@Data
public class Material {

    public enum Visibility {
        PUBLIC, ENROLLED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String filePath;
    private String fileType;

    @Enumerated(EnumType.STRING)
    private Visibility visibility;

    @CreationTimestamp
    private LocalDateTime uploadedAt;

//    @OneToMany(mappedBy = "material", cascade = CascadeType.ALL)
//    private List<Comment> comments; // via polymorphic mapping
}
