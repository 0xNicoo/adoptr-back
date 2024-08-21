package com.example.adoptr_backend.repository.specification;

import com.example.adoptr_backend.model.Province;
import com.example.adoptr_backend.service.dto.request.ProvinceFilterDTO;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Collection;

public class ProvinceSpec {
    public static Specification<Province> getSpec(ProvinceFilterDTO filter){
        return (root, query, cb) -> {
            final Collection<Predicate> predicates = new ArrayList<>();

            if (filter.getName() != null){
                predicates.add(cb.equal(root.get("name"), filter.getName()));
            }

            query.orderBy(cb.desc(root.get("id")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
