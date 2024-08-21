package com.example.adoptr_backend.repository.specification;

import com.example.adoptr_backend.model.Adoption;
import com.example.adoptr_backend.service.dto.request.AdoptionFilterDTO;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Collection;

public class AdoptionSpec {
    public static Specification<Adoption> getSpec(AdoptionFilterDTO filter){
        return (root, query, cb) -> {
            final Collection<Predicate> predicates = new ArrayList<>();

            if (filter.getSexType() != null){
                predicates.add(cb.equal(root.get("sexType"), filter.getSexType()));
            }

            if (filter.isVaccinated()){
                predicates.add(cb.equal(root.get("vaccinated"), true));
            }

            if (filter.isUnprotected()){
                predicates.add(cb.equal(root.get("unprotected"), true));
            }

            if (filter.isCastrated()){
                predicates.add(cb.equal(root.get("castrated"), true));
            }

            if (filter.getTitle() != null){
                predicates.add(cb.equal(root.get("title"), filter.getTitle()));
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

            if (filter.getAdoptionStatusType() != null){
                predicates.add(cb.equal(root.get("adoptionStatusType"), filter.getAdoptionStatusType()));
            }

            if (filter.getCreationDate() != null){
                predicates.add(cb.equal(root.get("creationDate"), filter.getCreationDate()));
            }

            query.orderBy(cb.desc(root.get("id")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
