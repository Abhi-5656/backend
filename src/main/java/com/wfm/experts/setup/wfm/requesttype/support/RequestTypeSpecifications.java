package com.wfm.experts.setup.wfm.requesttype.support;

import com.wfm.experts.setup.wfm.requesttype.entity.RequestType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Date;

public final class RequestTypeSpecifications {

    private RequestTypeSpecifications() {}

    /**
     * Creates a specification to find request types where the name contains the given string.
     */
    public static Specification<RequestType> nameContains(String name) {
        return (root, q, cb) ->
                name == null ? cb.conjunction() : cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    /**
     * Creates a specification to find request types with an exact effective date.
     */
    public static Specification<RequestType> effectiveDateEquals(Date date) {
        return (root, q, cb) ->
                date == null ? cb.conjunction() : cb.equal(root.get("effectiveDate"), date);
    }

    public static Specification<RequestType> effectiveDateFrom(LocalDate from) {
        return (root, q, cb) ->
                from == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("effectiveDate"), from);
    }

    public static Specification<RequestType> effectiveDateTo(LocalDate to) {
        return (root, q, cb) ->
                to == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.get("effectiveDate"), to);
    }

    /**
     * Active if: (effectiveDate is null or <= date) AND (expirationDate is null or >= date)
     */
    public static Specification<RequestType> activeOn(LocalDate date) {
        return (root, q, cb) -> {
            if (date == null) return cb.conjunction();
            return cb.and(
                    cb.or(cb.isNull(root.get("effectiveDate")),
                            cb.lessThanOrEqualTo(root.get("effectiveDate"), date)),
                    cb.or(cb.isNull(root.get("expirationDate")),
                            cb.greaterThanOrEqualTo(root.get("expirationDate"), date))
            );
        };
    }
}