package com.wfm.experts.setup.orgstructure.repository;

import com.wfm.experts.setup.orgstructure.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    List<Location> findByBusinessUnitId(Long businessUnitId);
    List<Location> findByParentId(Long parentId);
}
