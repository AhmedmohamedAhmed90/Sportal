package com.example.Sportal.models.entities;


import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "submissions")
@Data
public class Submission {

    public enum Status {
        SUBMITTED, LATE, RESUBMITTED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(nullable = false)
    private String filePath;

    @CreationTimestamp
    private LocalDateTime submittedAt;

    @Enumerated(EnumType.STRING)
    private Status status;

    private BigDecimal score;

    @Column(columnDefinition = "TEXT")
    private String feedback;

//    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL)
//    private List<Comment> comments;
}
