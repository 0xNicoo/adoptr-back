package com.adoptr.adoptr.location.repository;

import com.adoptr.adoptr.location.model.Locality;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LocalityRepository extends JpaRepository<Locality, Long> {

    /**
     * Localidades de la provincia cuyo nombre contiene el texto (ya normalizado, con
     * {@code \}, {@code %} y {@code _} escapados con {@code \}). 
     */
    @Query("""
            select l from Locality l
            where l.province.id = :provinceId and l.searchName like concat('%', :text, '%') escape '\\'
            order by case when l.searchName like concat(:text, '%') escape '\\' then 0 else 1 end, l.displayName
            """)
    List<Locality> search(@Param("provinceId") Long provinceId, @Param("text") String text, Pageable pageable);
}
