package com.example.adoptr_backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "publication_id", nullable = false)
    private Publication publication;

    @Column
    private LocalDateTime createdAt;

    @Column
    private Long adopterUserId; //TODO: cambiar este nombre, porque el chat no solo va en adopcion. POR AHORA SE TRATA COMO INTERLOCUTOR EN ALGUNAS PARTES.

    @Column
    private Long publicationUserId;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> messages;

    @PrePersist
    public void prePersist(){
        this.messages = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
    }
}
