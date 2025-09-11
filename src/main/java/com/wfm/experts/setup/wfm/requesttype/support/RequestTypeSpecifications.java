package com.wfm.experts.setup.wfm.requesttype.support;

import com.wfm.experts.setup.wfm.requesttype.entity.RequestType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public final class RequestTypeSpecifications {

    private RequestTypeSpecifications() {}

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
