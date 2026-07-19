package com.example.guarantees.repository;

import com.example.guarantees.domain.Guarantee;
import com.example.guarantees.domain.GuaranteeStatus;
import com.example.guarantees.domain.GuaranteeType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GuaranteeRepository extends JpaRepository<Guarantee, Long> {
    List<Guarantee> findByStatus(GuaranteeStatus status);
    List<Guarantee> findByType(GuaranteeType type);
    List<Guarantee> findByStatusAndType(GuaranteeStatus status, GuaranteeType type);
    List<Guarantee> findByStatusInAndExpiryDateLessThanEqual(List<GuaranteeStatus> statuses, LocalDate date);
    List<Guarantee> findByExpiryDateBetweenAndStatusNotInOrderByExpiryDateAscReferenceAsc(
            LocalDate startDate,
            LocalDate endDate,
            List<GuaranteeStatus> excludedStatuses);

    @Query("SELECT g.status, COUNT(g) FROM Guarantee g GROUP BY g.status")
    List<Object[]> countByStatus();

    @Query("SELECT g.type, COUNT(g) FROM Guarantee g GROUP BY g.type")
    List<Object[]> countByType();

    @Query("SELECT YEAR(g.issueDate), MONTH(g.issueDate), COUNT(g) FROM Guarantee g GROUP BY YEAR(g.issueDate), MONTH(g.issueDate) ORDER BY YEAR(g.issueDate), MONTH(g.issueDate)")
    List<Object[]> countByMonth();

    @Query("""
        SELECT COUNT(g)
        FROM Guarantee g
        WHERE (:status IS NULL OR g.status = :status)
          AND (:type IS NULL OR g.type = :type)
          AND (:currency IS NULL OR UPPER(g.currency) = UPPER(:currency))
          AND (:issueDateFrom IS NULL OR g.issueDate >= :issueDateFrom)
          AND (:issueDateTo IS NULL OR g.issueDate <= :issueDateTo)
          AND (:expiryDateFrom IS NULL OR g.expiryDate >= :expiryDateFrom)
          AND (:expiryDateTo IS NULL OR g.expiryDate <= :expiryDateTo)
        """)
    long countFiltered(@Param("status") GuaranteeStatus status,
                       @Param("type") GuaranteeType type,
                       @Param("currency") String currency,
                       @Param("issueDateFrom") LocalDate issueDateFrom,
                       @Param("issueDateTo") LocalDate issueDateTo,
                       @Param("expiryDateFrom") LocalDate expiryDateFrom,
                       @Param("expiryDateTo") LocalDate expiryDateTo);

    @Query("""
        SELECT g.status, COUNT(g)
        FROM Guarantee g
        WHERE (:status IS NULL OR g.status = :status)
          AND (:type IS NULL OR g.type = :type)
          AND (:currency IS NULL OR UPPER(g.currency) = UPPER(:currency))
          AND (:issueDateFrom IS NULL OR g.issueDate >= :issueDateFrom)
          AND (:issueDateTo IS NULL OR g.issueDate <= :issueDateTo)
          AND (:expiryDateFrom IS NULL OR g.expiryDate >= :expiryDateFrom)
          AND (:expiryDateTo IS NULL OR g.expiryDate <= :expiryDateTo)
        GROUP BY g.status
        ORDER BY g.status
        """)
    List<Object[]> countByStatusFiltered(@Param("status") GuaranteeStatus status,
                                         @Param("type") GuaranteeType type,
                                         @Param("currency") String currency,
                                         @Param("issueDateFrom") LocalDate issueDateFrom,
                                         @Param("issueDateTo") LocalDate issueDateTo,
                                         @Param("expiryDateFrom") LocalDate expiryDateFrom,
                                         @Param("expiryDateTo") LocalDate expiryDateTo);

    @Query("""
        SELECT g.type, COUNT(g)
        FROM Guarantee g
        WHERE (:status IS NULL OR g.status = :status)
          AND (:type IS NULL OR g.type = :type)
          AND (:currency IS NULL OR UPPER(g.currency) = UPPER(:currency))
          AND (:issueDateFrom IS NULL OR g.issueDate >= :issueDateFrom)
          AND (:issueDateTo IS NULL OR g.issueDate <= :issueDateTo)
          AND (:expiryDateFrom IS NULL OR g.expiryDate >= :expiryDateFrom)
          AND (:expiryDateTo IS NULL OR g.expiryDate <= :expiryDateTo)
        GROUP BY g.type
        ORDER BY g.type
        """)
    List<Object[]> countByTypeFiltered(@Param("status") GuaranteeStatus status,
                                       @Param("type") GuaranteeType type,
                                       @Param("currency") String currency,
                                       @Param("issueDateFrom") LocalDate issueDateFrom,
                                       @Param("issueDateTo") LocalDate issueDateTo,
                                       @Param("expiryDateFrom") LocalDate expiryDateFrom,
                                       @Param("expiryDateTo") LocalDate expiryDateTo);

    @Query("""
        SELECT g.currency, COUNT(g)
        FROM Guarantee g
        WHERE (:status IS NULL OR g.status = :status)
          AND (:type IS NULL OR g.type = :type)
          AND (:currency IS NULL OR UPPER(g.currency) = UPPER(:currency))
          AND (:issueDateFrom IS NULL OR g.issueDate >= :issueDateFrom)
          AND (:issueDateTo IS NULL OR g.issueDate <= :issueDateTo)
          AND (:expiryDateFrom IS NULL OR g.expiryDate >= :expiryDateFrom)
          AND (:expiryDateTo IS NULL OR g.expiryDate <= :expiryDateTo)
        GROUP BY g.currency
        ORDER BY g.currency
        """)
    List<Object[]> countByCurrencyFiltered(@Param("status") GuaranteeStatus status,
                                           @Param("type") GuaranteeType type,
                                           @Param("currency") String currency,
                                           @Param("issueDateFrom") LocalDate issueDateFrom,
                                           @Param("issueDateTo") LocalDate issueDateTo,
                                           @Param("expiryDateFrom") LocalDate expiryDateFrom,
                                           @Param("expiryDateTo") LocalDate expiryDateTo);

    @Query("""
        SELECT YEAR(g.issueDate), MONTH(g.issueDate), COUNT(g)
        FROM Guarantee g
        WHERE (:status IS NULL OR g.status = :status)
          AND (:type IS NULL OR g.type = :type)
          AND (:currency IS NULL OR UPPER(g.currency) = UPPER(:currency))
          AND (:issueDateFrom IS NULL OR g.issueDate >= :issueDateFrom)
          AND (:issueDateTo IS NULL OR g.issueDate <= :issueDateTo)
          AND (:expiryDateFrom IS NULL OR g.expiryDate >= :expiryDateFrom)
          AND (:expiryDateTo IS NULL OR g.expiryDate <= :expiryDateTo)
        GROUP BY YEAR(g.issueDate), MONTH(g.issueDate)
        ORDER BY YEAR(g.issueDate), MONTH(g.issueDate)
        """)
    List<Object[]> countByMonthFiltered(@Param("status") GuaranteeStatus status,
                                        @Param("type") GuaranteeType type,
                                        @Param("currency") String currency,
                                        @Param("issueDateFrom") LocalDate issueDateFrom,
                                        @Param("issueDateTo") LocalDate issueDateTo,
                                        @Param("expiryDateFrom") LocalDate expiryDateFrom,
                                        @Param("expiryDateTo") LocalDate expiryDateTo);

    @Query("""
        SELECT COALESCE(SUM(g.amount), 0)
        FROM Guarantee g
        WHERE (:status IS NULL OR g.status = :status)
          AND (:type IS NULL OR g.type = :type)
          AND (:currency IS NULL OR UPPER(g.currency) = UPPER(:currency))
          AND (:issueDateFrom IS NULL OR g.issueDate >= :issueDateFrom)
          AND (:issueDateTo IS NULL OR g.issueDate <= :issueDateTo)
          AND (:expiryDateFrom IS NULL OR g.expiryDate >= :expiryDateFrom)
          AND (:expiryDateTo IS NULL OR g.expiryDate <= :expiryDateTo)
        """)
    java.math.BigDecimal sumAmountFiltered(@Param("status") GuaranteeStatus status,
                                           @Param("type") GuaranteeType type,
                                           @Param("currency") String currency,
                                           @Param("issueDateFrom") LocalDate issueDateFrom,
                                           @Param("issueDateTo") LocalDate issueDateTo,
                                           @Param("expiryDateFrom") LocalDate expiryDateFrom,
                                           @Param("expiryDateTo") LocalDate expiryDateTo);

    @Query("""
        SELECT UPPER(g.currency), COALESCE(SUM(g.amount), 0)
        FROM Guarantee g
        WHERE (:status IS NULL OR g.status = :status)
          AND (:type IS NULL OR g.type = :type)
          AND (:currency IS NULL OR UPPER(g.currency) = UPPER(:currency))
          AND (:issueDateFrom IS NULL OR g.issueDate >= :issueDateFrom)
          AND (:issueDateTo IS NULL OR g.issueDate <= :issueDateTo)
          AND (:expiryDateFrom IS NULL OR g.expiryDate >= :expiryDateFrom)
          AND (:expiryDateTo IS NULL OR g.expiryDate <= :expiryDateTo)
        GROUP BY UPPER(g.currency)
        ORDER BY UPPER(g.currency)
        """)
    List<Object[]> sumAmountByCurrencyFiltered(@Param("status") GuaranteeStatus status,
                                               @Param("type") GuaranteeType type,
                                               @Param("currency") String currency,
                                               @Param("issueDateFrom") LocalDate issueDateFrom,
                                               @Param("issueDateTo") LocalDate issueDateTo,
                                               @Param("expiryDateFrom") LocalDate expiryDateFrom,
                                               @Param("expiryDateTo") LocalDate expiryDateTo);

    @Query("""
        SELECT b.id, b.firstName, b.lastName, b.taxId, COUNT(g), COALESCE(SUM(g.amount), 0)
        FROM Guarantee g
        JOIN g.beneficiary b
        WHERE (:status IS NULL OR g.status = :status)
          AND (:type IS NULL OR g.type = :type)
          AND (:currency IS NULL OR UPPER(g.currency) = UPPER(:currency))
          AND (:issueDateFrom IS NULL OR g.issueDate >= :issueDateFrom)
          AND (:issueDateTo IS NULL OR g.issueDate <= :issueDateTo)
          AND (:expiryDateFrom IS NULL OR g.expiryDate >= :expiryDateFrom)
          AND (:expiryDateTo IS NULL OR g.expiryDate <= :expiryDateTo)
        GROUP BY b.id, b.firstName, b.lastName, b.taxId
        ORDER BY COUNT(g) DESC, COALESCE(SUM(g.amount), 0) DESC, b.lastName ASC
        """)
    List<Object[]> findTopBeneficiariesFiltered(@Param("status") GuaranteeStatus status,
                                                @Param("type") GuaranteeType type,
                                                @Param("currency") String currency,
                                                @Param("issueDateFrom") LocalDate issueDateFrom,
                                                @Param("issueDateTo") LocalDate issueDateTo,
                                                @Param("expiryDateFrom") LocalDate expiryDateFrom,
                                                @Param("expiryDateTo") LocalDate expiryDateTo,
                                                Pageable pageable);

    @Query("""
        SELECT COUNT(g)
        FROM Guarantee g
        WHERE g.status IN :statuses
          AND (:status IS NULL OR g.status = :status)
          AND (:type IS NULL OR g.type = :type)
          AND (:currency IS NULL OR UPPER(g.currency) = UPPER(:currency))
          AND (:issueDateFrom IS NULL OR g.issueDate >= :issueDateFrom)
          AND (:issueDateTo IS NULL OR g.issueDate <= :issueDateTo)
          AND (:expiryDateFrom IS NULL OR g.expiryDate >= :expiryDateFrom)
          AND (:expiryDateTo IS NULL OR g.expiryDate <= :expiryDateTo)
        """)
    long countFilteredByStatuses(@Param("statuses") List<GuaranteeStatus> statuses,
                                 @Param("status") GuaranteeStatus status,
                                 @Param("type") GuaranteeType type,
                                 @Param("currency") String currency,
                                 @Param("issueDateFrom") LocalDate issueDateFrom,
                                 @Param("issueDateTo") LocalDate issueDateTo,
                                 @Param("expiryDateFrom") LocalDate expiryDateFrom,
                                 @Param("expiryDateTo") LocalDate expiryDateTo);

    @Query("""
        SELECT COUNT(g)
        FROM Guarantee g
        WHERE g.expiryDate BETWEEN :fromDate AND :toDate
          AND (:status IS NULL OR g.status = :status)
          AND (:type IS NULL OR g.type = :type)
          AND (:currency IS NULL OR UPPER(g.currency) = UPPER(:currency))
          AND (:issueDateFrom IS NULL OR g.issueDate >= :issueDateFrom)
          AND (:issueDateTo IS NULL OR g.issueDate <= :issueDateTo)
          AND (:expiryDateFrom IS NULL OR g.expiryDate >= :expiryDateFrom)
          AND (:expiryDateTo IS NULL OR g.expiryDate <= :expiryDateTo)
        """)
    long countExpiringBetweenFiltered(@Param("fromDate") LocalDate fromDate,
                                      @Param("toDate") LocalDate toDate,
                                      @Param("status") GuaranteeStatus status,
                                      @Param("type") GuaranteeType type,
                                      @Param("currency") String currency,
                                      @Param("issueDateFrom") LocalDate issueDateFrom,
                                      @Param("issueDateTo") LocalDate issueDateTo,
                                      @Param("expiryDateFrom") LocalDate expiryDateFrom,
                                      @Param("expiryDateTo") LocalDate expiryDateTo);
}
