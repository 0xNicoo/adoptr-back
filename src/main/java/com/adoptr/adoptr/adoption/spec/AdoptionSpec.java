package com.adoptr.adoptr.adoption.spec;

import com.adoptr.adoptr.adoption.dto.request.AdoptionFilterDTO;
import com.adoptr.adoptr.adoption.model.AdoptionPublication;
import com.adoptr.adoptr.adoption.model.AdoptionStatus;
import com.adoptr.adoptr.adoption.model.LifeStage;
import com.adoptr.adoptr.location.model.Locality;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public final class AdoptionSpec {

    private AdoptionSpec() {
    }

    /**
     * Catálogo público: solo publicaciones en adopción y no borradas, más los filtros que vengan.
     * Trae localidad y provincia en la misma consulta (salvo en la de conteo).
     */
    @SuppressWarnings("unchecked")
    public static Specification<AdoptionPublication> catalog(AdoptionFilterDTO filter, YearMonth current) {
        return (root, query, cb) -> {
            Join<AdoptionPublication, Locality> locality;
            if (query.getResultType() == Long.class || query.getResultType() == long.class) {
                locality = root.join("locality", JoinType.INNER);
            } else {
                Fetch<AdoptionPublication, Locality> fetch = root.fetch("locality", JoinType.INNER);
                fetch.fetch("province", JoinType.INNER);
                locality = (Join<AdoptionPublication, Locality>) fetch;
            }

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isNull(root.get("deletedAt")));
            predicates.add(cb.equal(root.get("status"), AdoptionStatus.AVAILABLE));
            if (filter.getSpecies() != null) {
                predicates.add(cb.equal(root.get("species"), filter.getSpecies()));
            }
            if (filter.getSex() != null) {
                predicates.add(cb.equal(root.get("sex"), filter.getSex()));
            }
            if (filter.getSize() != null) {
                predicates.add(cb.equal(root.get("size"), filter.getSize()));
            }
            if (filter.getLocalityId() != null) {
                predicates.add(cb.equal(locality.get("id"), filter.getLocalityId()));
            }
            if (filter.getProvinceId() != null) {
                predicates.add(cb.equal(locality.get("province").get("id"), filter.getProvinceId()));
            }
            if (filter.getLifeStage() != null) {
                LifeStage.BirthDateRange range = filter.getLifeStage().birthDateRange(current);
                if (range.from() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("estimatedBirthDate"), range.from()));
                }
                if (range.to() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("estimatedBirthDate"), range.to()));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
