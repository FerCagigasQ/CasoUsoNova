package com.example.guarantees.repository;

import com.example.guarantees.domain.Guarantee;
import com.example.guarantees.domain.GuaranteeStatus;
import com.example.guarantees.domain.GuaranteeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GuaranteeRepository extends JpaRepository<Guarantee, Long> {
    List<Guarantee> findByStatus(GuaranteeStatus status);
    List<Guarantee> findByType(GuaranteeType type);
    List<Guarantee> findByStatusAndType(GuaranteeStatus status, GuaranteeType type);
}
