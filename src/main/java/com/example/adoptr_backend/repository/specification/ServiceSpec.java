package com.example.adoptr_backend.repository.specification;

import com.example.adoptr_backend.model.Service;
import com.example.adoptr_backend.service.dto.request.ServiceFilterDTO;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Collection;

public class ServiceSpec {
    public static Specification<Service> getSpec(ServiceFilterDTO filter){
        return (root, query, cb) -> {
            final Collection<Predicate> predicates = new ArrayList<>();

            if (filter.getTitle() != null){
                predicates.add(cb.equal(root.get("title"), filter.getTitle()));
            }

            if (filter.getCreationDate() != null){
                predicates.add(cb.equal(root.get("creationDate"), filter.getCreationDate()));
            }

            if (filter.getStreet() != null){
                predicates.add(cb.equal(root.get("street"), filter.getStreet()));
            }

            if (filter.getNumber() > 0){
                predicates.add(cb.equal(root.get("number"), filter.getNumber()));
            }

            if (filter.getServiceType_id() != null){
                predicates.add(cb.equal(root.get("serviceType").get("id"), filter.getServiceType_id()));
            }

            if (filter.getLocality_id() != null){
                predicates.add(cb.equal(root.get("locality").get("id"), filter.getLocality_id()));
            }

            if (filter.getUser_id() != null){
                predicates.add(cb.equal(root.get("user").get("id"), filter.getUser_id()));
            }

            query.orderBy(cb.desc(root.get("id")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
