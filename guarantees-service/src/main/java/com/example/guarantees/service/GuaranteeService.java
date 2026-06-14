package com.example.guarantees.service;

import com.example.guarantees.domain.*;
import com.example.guarantees.dto.*;
import com.example.guarantees.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class GuaranteeService {
    private final GuaranteeRepository guaranteeRepository;
    private final ApplicantRepository applicantRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final IssuingBankRepository issuingBankRepository;
    private final AmendmentRepository amendmentRepository;
    private final ClaimRepository claimRepository;

    public GuaranteeService(GuaranteeRepository guaranteeRepository,
                           ApplicantRepository applicantRepository,
                           BeneficiaryRepository beneficiaryRepository,
                           IssuingBankRepository issuingBankRepository,
                           AmendmentRepository amendmentRepository,
                           ClaimRepository claimRepository) {
        this.guaranteeRepository = guaranteeRepository;
        this.applicantRepository = applicantRepository;
        this.beneficiaryRepository = beneficiaryRepository;
        this.issuingBankRepository = issuingBankRepository;
        this.amendmentRepository = amendmentRepository;
        this.claimRepository = claimRepository;
    }

    public GuaranteeDTO createGuarantee(GuaranteeDTO dto) {
        Applicant applicant = applicantRepository.findById(dto.getApplicant().getId())
            .orElseThrow(() -> new IllegalArgumentException("Applicant not found"));
        Beneficiary beneficiary = beneficiaryRepository.findById(dto.getBeneficiary().getId())
            .orElseThrow(() -> new IllegalArgumentException("Beneficiary not found"));
        IssuingBank bank = issuingBankRepository.findById(dto.getIssuingBank().getId())
            .orElseThrow(() -> new IllegalArgumentException("IssuingBank not found"));

        Guarantee guarantee = new Guarantee();
        guarantee.setReferenceNumber(dto.getReferenceNumber());
        guarantee.setApplicant(applicant);
        guarantee.setBeneficiary(beneficiary);
        guarantee.setIssuingBank(bank);
        guarantee.setAmount(dto.getAmount());
        guarantee.setIssueDate(dto.getIssueDate());
        guarantee.setExpiryDate(dto.getExpiryDate());
        guarantee.setStatus(GuaranteeStatus.valueOf(dto.getStatus()));
        guarantee.setDescription(dto.getDescription());

        Guarantee saved = guaranteeRepository.save(guarantee);
        return toDTO(saved);
    }

    public Optional<GuaranteeDTO> getGuaranteeById(Long id) {
        return guaranteeRepository.findById(id).map(this::toDTO);
    }

    public List<GuaranteeDTO> getAllGuarantees() {
        return guaranteeRepository.findAll().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    public GuaranteeDTO updateGuarantee(Long id, GuaranteeDTO dto) {
        Guarantee guarantee = guaranteeRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Guarantee not found"));

        guarantee.setAmount(dto.getAmount());
        guarantee.setExpiryDate(dto.getExpiryDate());
        guarantee.setStatus(GuaranteeStatus.valueOf(dto.getStatus()));
        guarantee.setDescription(dto.getDescription());

        Guarantee updated = guaranteeRepository.save(guarantee);
        return toDTO(updated);
    }

    public void deleteGuarantee(Long id) {
        guaranteeRepository.deleteById(id);
    }

    public GuaranteeDTO issueGuarantee(Long id) {
        Guarantee guarantee = guaranteeRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Guarantee not found"));
        guarantee.setStatus(GuaranteeStatus.ACTIVE);
        guarantee.setIssueDate(LocalDate.now());
        Guarantee updated = guaranteeRepository.save(guarantee);
        return toDTO(updated);
    }

    public AmendmentDTO amendGuarantee(Long guaranteeId, AmendmentDTO dto) {
        Guarantee guarantee = guaranteeRepository.findById(guaranteeId)
            .orElseThrow(() -> new IllegalArgumentException("Guarantee not found"));

        Amendment amendment = new Amendment();
        amendment.setGuarantee(guarantee);
        amendment.setAmendmentDate(dto.getAmendmentDate());
        amendment.setDescription(dto.getDescription());
        amendment.setNewAmount(dto.getNewAmount());
        amendment.setNewExpiryDate(dto.getNewExpiryDate());

        if (dto.getNewAmount() != null) {
            guarantee.setAmount(dto.getNewAmount());
        }
        if (dto.getNewExpiryDate() != null) {
            guarantee.setExpiryDate(dto.getNewExpiryDate());
        }
        guarantee.setStatus(GuaranteeStatus.AMENDED);

        Amendment saved = amendmentRepository.save(amendment);
        guaranteeRepository.save(guarantee);
        return toAmendmentDTO(saved);
    }

    public ClaimDTO submitClaim(Long guaranteeId, ClaimDTO dto) {
        Guarantee guarantee = guaranteeRepository.findById(guaranteeId)
            .orElseThrow(() -> new IllegalArgumentException("Guarantee not found"));

        Claim claim = new Claim();
        claim.setGuarantee(guarantee);
        claim.setSubmissionDate(dto.getSubmissionDate());
        claim.setClaimedAmount(dto.getClaimedAmount());
        claim.setStatus(ClaimStatus.valueOf(dto.getStatus()));
        claim.setReason(dto.getReason());

        guarantee.setStatus(GuaranteeStatus.CLAIMED);

        Claim saved = claimRepository.save(claim);
        guaranteeRepository.save(guarantee);
        return toClaimDTO(saved);
    }

    public List<GuaranteeDTO> getGuaranteesByStatus(String status) {
        return guaranteeRepository.findByStatus(GuaranteeStatus.valueOf(status))
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    public List<AmendmentDTO> getAmendmentsForGuarantee(Long guaranteeId) {
        return amendmentRepository.findByGuaranteeId(guaranteeId)
            .stream()
            .map(this::toAmendmentDTO)
            .collect(Collectors.toList());
    }

    public List<ClaimDTO> getClaimsForGuarantee(Long guaranteeId) {
        return claimRepository.findByGuaranteeId(guaranteeId)
            .stream()
            .map(this::toClaimDTO)
            .collect(Collectors.toList());
    }

    private GuaranteeDTO toDTO(Guarantee guarantee) {
        return new GuaranteeDTO(
            guarantee.getId(),
            guarantee.getReferenceNumber(),
            new ApplicantDTO(
                guarantee.getApplicant().getId(),
                guarantee.getApplicant().getFirstName(),
                guarantee.getApplicant().getLastName(),
                guarantee.getApplicant().getTaxId(),
                guarantee.getApplicant().getEmail(),
                guarantee.getApplicant().getPhone()
            ),
            new BeneficiaryDTO(
                guarantee.getBeneficiary().getId(),
                guarantee.getBeneficiary().getFirstName(),
                guarantee.getBeneficiary().getLastName(),
                guarantee.getBeneficiary().getTaxId(),
                guarantee.getBeneficiary().getEmail(),
                guarantee.getBeneficiary().getPhone()
            ),
            new IssuingBankDTO(
                guarantee.getIssuingBank().getId(),
                guarantee.getIssuingBank().getCode(),
                guarantee.getIssuingBank().getName(),
                guarantee.getIssuingBank().getCountry()
            ),
            guarantee.getAmount(),
            guarantee.getIssueDate(),
            guarantee.getExpiryDate(),
            guarantee.getStatus().name(),
            guarantee.getDescription()
        );
    }

    private AmendmentDTO toAmendmentDTO(Amendment amendment) {
        return new AmendmentDTO(
            amendment.getId(),
            amendment.getGuarantee().getId(),
            amendment.getAmendmentDate(),
            amendment.getDescription(),
            amendment.getNewAmount(),
            amendment.getNewExpiryDate(),
            amendment.getCreatedAt()
        );
    }

    private ClaimDTO toClaimDTO(Claim claim) {
        return new ClaimDTO(
            claim.getId(),
            claim.getGuarantee().getId(),
            claim.getSubmissionDate(),
            claim.getClaimedAmount(),
            claim.getStatus().name(),
            claim.getReason(),
            claim.getReviewNotes(),
            claim.getResolutionDate(),
            claim.getCreatedAt()
        );
    }
}
