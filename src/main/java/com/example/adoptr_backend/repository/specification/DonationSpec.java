package com.example.adoptr_backend.repository.specification;

import com.example.adoptr_backend.model.Donation;
import com.example.adoptr_backend.service.dto.request.DonationFilterDTO;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Collection;

public class DonationSpec {
    public static Specification<Donation> getSpec(DonationFilterDTO filter) {
        return (root, query, cb) -> {
            final Collection<Predicate> predicates = new ArrayList<>();

            if (filter.getAmount() != null) {
                predicates.add(cb.equal(root.get("amount"), filter.getAmount()));
            }
            if (filter.getDescription() != null) {
                predicates.add(cb.like(cb.lower(root.get("description")), "%" + filter.getDescription().toLowerCase() + "%"));
            }
            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }
            if (filter.getPaymentMethod() != null) {
                predicates.add(cb.equal(root.get("paymentMethod"), filter.getPaymentMethod()));
            }
            if (filter.getApprovalDate() != null) {
                predicates.add(cb.equal(root.get("approvalDate"), filter.getApprovalDate()));
            }

            query.orderBy(cb.desc(root.get("id")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

