package com.example.guarantees.repository;

import com.example.guarantees.domain.Guarantee;
import com.example.guarantees.domain.GuaranteeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface GuaranteeRepository extends JpaRepository<Guarantee, Long> {
    Optional<Guarantee> findByReferenceNumber(String referenceNumber);
    List<Guarantee> findByStatus(GuaranteeStatus status);
    List<Guarantee> findByApplicantId(Long applicantId);
    List<Guarantee> findByBeneficiaryId(Long beneficiaryId);
}
