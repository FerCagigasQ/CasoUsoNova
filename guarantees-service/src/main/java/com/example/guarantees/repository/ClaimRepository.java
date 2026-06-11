package com.example.guarantees.repository;

import com.example.guarantees.domain.Claim;
import com.example.guarantees.domain.ClaimStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {
    List<Claim> findByGuaranteeId(Long guaranteeId);
    List<Claim> findByStatus(ClaimStatus status);
}
