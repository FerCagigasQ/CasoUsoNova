package com.example.guarantees.repository;

import com.example.guarantees.domain.IssuingBank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IssuingBankRepository extends JpaRepository<IssuingBank, Long> {
}
