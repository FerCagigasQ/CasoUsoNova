package com.example.guarantees.repository;

import com.example.guarantees.domain.Amendment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AmendmentRepository extends JpaRepository<Amendment, Long> {
    List<Amendment> findByGuaranteeId(Long guaranteeId);
}
