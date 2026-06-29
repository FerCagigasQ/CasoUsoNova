package com.example.guarantees.repository;

import com.example.guarantees.domain.Guarantee;
import com.example.guarantees.domain.GuaranteeStatus;
import com.example.guarantees.domain.GuaranteeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GuaranteeRepository extends JpaRepository<Guarantee, Long> {
    List<Guarantee> findByStatus(GuaranteeStatus status);
    List<Guarantee> findByType(GuaranteeType type);
    List<Guarantee> findByStatusAndType(GuaranteeStatus status, GuaranteeType type);

    @Query("SELECT g.status, COUNT(g) FROM Guarantee g GROUP BY g.status")
    List<Object[]> countByStatus();

    @Query("SELECT g.type, COUNT(g) FROM Guarantee g GROUP BY g.type")
    List<Object[]> countByType();

    @Query("SELECT YEAR(g.issueDate), MONTH(g.issueDate), COUNT(g) FROM Guarantee g GROUP BY YEAR(g.issueDate), MONTH(g.issueDate) ORDER BY YEAR(g.issueDate), MONTH(g.issueDate)")
    List<Object[]> countByMonth();
}
