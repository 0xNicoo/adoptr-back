package com.example.adoptr_backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Publication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String title;

    @Enumerated(EnumType.STRING)
    private PublicationType type;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column
    private LocalDateTime creationDate;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id")
    private User user;

    @OneToMany(mappedBy = "publication", cascade = CascadeType.ALL)
    private List<Chat> chats;

    @Column
    private Long imageId;

    @ManyToOne
    @JoinColumn(name = "locality_id")
    private Locality locality;

    public void addChat(Chat chat){
        this.chats.add(chat);
    }

}
