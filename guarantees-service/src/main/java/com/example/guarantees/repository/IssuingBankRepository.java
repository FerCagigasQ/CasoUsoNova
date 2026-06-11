package com.example.guarantees.repository;

import com.example.guarantees.domain.IssuingBank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface IssuingBankRepository extends JpaRepository<IssuingBank, Long> {
    Optional<IssuingBank> findByCode(String code);
}
