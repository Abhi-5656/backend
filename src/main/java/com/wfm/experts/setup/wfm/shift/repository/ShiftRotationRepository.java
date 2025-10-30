package com.wfm.experts.setup.wfm.shift.repository;

import com.wfm.experts.setup.wfm.shift.entity.ShiftRotation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShiftRotationRepository extends JpaRepository<ShiftRotation, Long> {
    Optional<ShiftRotation> findByName(String name);
}