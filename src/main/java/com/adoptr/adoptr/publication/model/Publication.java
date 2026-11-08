package com.adoptr.adoptr.publication.model;

import com.adoptr.adoptr.location.model.Locality;
import com.adoptr.adoptr.user.model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Base común de las publicaciones (adopción y, más adelante, perdidas y servicios).
 * Las fechas y el tipo los decide la capa de servicio; no hay valores por defecto en la entidad.
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
public abstract class Publication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PublicationType type;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    /**
     * Quien publica. La columna sigue siendo {@code user_id} para no romper bases existentes.
     * LAZY: los listados no necesitan el usuario (ni su perfil, que el usuario trae EAGER).
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    /** LAZY: las consultas que la muestran la traen con fetch o con un entity graph. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locality_id")
    private Locality locality;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /** {@code null} mientras la publicación está activa; con valor, fue dada de baja (borrado lógico). */
    private LocalDateTime deletedAt;
}
