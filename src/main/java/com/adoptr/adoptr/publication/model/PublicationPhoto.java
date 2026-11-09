package com.adoptr.adoptr.publication.model;

import com.adoptr.adoptr.shared.storage.StoredImage;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

/** Foto de una publicación. La de menor {@code sortOrder} es la portada. */
@Entity
@Table(name = "publication_photo",
        uniqueConstraints = @UniqueConstraint(columnNames = {"publication_id", "sort_order"}))
@Getter
@Setter
public class PublicationPhoto extends StoredImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "publication_id", nullable = false)
    private Publication publication;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;
}
