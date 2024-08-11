package com.example.adoptr_backend.repository.specification;

import com.example.adoptr_backend.model.Example;
import com.example.adoptr_backend.service.dto.request.ExampleFilterDTO;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Collection;

public class ExampleSpec {
    public static Specification<Example> getSpec(ExampleFilterDTO filter){
        return (root, query, cb) -> {
            final Collection<Predicate> predicates = new ArrayList<>();

            if (filter.getTitle() != null){
                predicates.add(cb.equal(root.get("title"), filter.getTitle()));
            }

            if (filter.getText() != null){
                String likePattern = "%" + filter.getText() + "%";
                predicates.add(cb.like(root.get("text"), likePattern));
            }

            if (filter.getActive() != null){
                predicates.add(cb.equal(root.get("active"), filter.getActive()));
            }

            if (filter.getType() != null){
                predicates.add(cb.equal(root.get("type"), filter.getType()));
            }

            query.orderBy(cb.desc(root.get("id")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
