package com.example.guarantees.repository;

import com.example.guarantees.domain.Amendment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AmendmentRepository extends JpaRepository<Amendment, Long> {
}
