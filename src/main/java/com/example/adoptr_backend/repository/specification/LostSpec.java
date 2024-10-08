package com.example.adoptr_backend.repository.specification;

import com.example.adoptr_backend.model.AdoptionStatusType;
import com.example.adoptr_backend.model.Lost;
import com.example.adoptr_backend.service.dto.request.LostFilterDTO;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Collection;

public class LostSpec {
    public static Specification<Lost> getSpec(LostFilterDTO filter){
        return (root, query, cb) -> {
            final Collection<Predicate> predicates = new ArrayList<>();

            if (filter.getTitle() != null){
                predicates.add(cb.equal(root.get("title"), filter.getTitle()));
            }

            if (filter.getCreationDate() != null){
                predicates.add(cb.equal(root.get("creationDate"), filter.getCreationDate()));
            }

            if (filter.getSexType() != null){
                predicates.add(cb.equal(root.get("sexType"), filter.getSexType()));
            }

            if (filter.getAnimalType() != null){
                predicates.add(cb.equal(root.get("animalType"), filter.getAnimalType()));
            }

            if (filter.getSizeType() != null){
                predicates.add(cb.equal(root.get("sizeType"), filter.getSizeType()));
            }

            if (filter.getAgeYears() > 0){
                predicates.add(cb.equal(root.get("ageYears"), filter.getAgeYears()));
            }

            if (filter.getLostStatusType() != null){
                predicates.add(cb.equal(root.get("lostStatusType"), filter.getLostStatusType()));
            }

            query.orderBy(cb.desc(root.get("id")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
