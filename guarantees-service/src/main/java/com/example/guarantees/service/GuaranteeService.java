package com.example.guarantees.service;

import com.example.guarantees.domain.Amendment;
import com.example.guarantees.domain.Applicant;
import com.example.guarantees.domain.Beneficiary;
import com.example.guarantees.domain.Claim;
import com.example.guarantees.domain.ClaimStatus;
import com.example.guarantees.domain.Guarantee;
import com.example.guarantees.domain.GuaranteeStatus;
import com.example.guarantees.domain.GuaranteeType;
import com.example.guarantees.domain.IssuingBank;
import com.example.guarantees.dto.AmendmentDTO;
import com.example.guarantees.dto.ApplicantDTO;
import com.example.guarantees.dto.BeneficiaryDTO;
import com.example.guarantees.dto.ClaimDTO;
import com.example.guarantees.dto.CreateGuaranteeRequest;
import com.example.guarantees.dto.GuaranteeDTO;
import com.example.guarantees.dto.IssuingBankDTO;
import com.example.guarantees.repository.AmendmentRepository;
import com.example.guarantees.repository.ApplicantRepository;
import com.example.guarantees.repository.BeneficiaryRepository;
import com.example.guarantees.repository.ClaimRepository;
import com.example.guarantees.repository.GuaranteeRepository;
import com.example.guarantees.repository.IssuingBankRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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
    private final GuaranteeEventService guaranteeEventService;

    public GuaranteeService(GuaranteeRepository guaranteeRepository, ApplicantRepository applicantRepository, BeneficiaryRepository beneficiaryRepository, IssuingBankRepository issuingBankRepository, AmendmentRepository amendmentRepository, ClaimRepository claimRepository, GuaranteeEventService guaranteeEventService) {
        this.guaranteeRepository = guaranteeRepository;
        this.applicantRepository = applicantRepository;
        this.beneficiaryRepository = beneficiaryRepository;
        this.issuingBankRepository = issuingBankRepository;
        this.amendmentRepository = amendmentRepository;
        this.claimRepository = claimRepository;
        this.guaranteeEventService = guaranteeEventService;
    }

    public List<GuaranteeDTO> findAll(GuaranteeStatus status, GuaranteeType type) {
        List<Guarantee> guarantees;

        if (status != null && type != null) {
            guarantees = guaranteeRepository.findByStatusAndType(status, type);
        } else if (status != null) {
            guarantees = guaranteeRepository.findByStatus(status);
        } else if (type != null) {
            guarantees = guaranteeRepository.findByType(type);
        } else {
            guarantees = guaranteeRepository.findAll();
        }

        return guarantees.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public GuaranteeDTO findById(Long id) {
        return guaranteeRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("Guarantee not found with id: " + id));
    }

    @CacheEvict(value = "metrics", allEntries = true)
    public GuaranteeDTO create(CreateGuaranteeRequest request) {
        Applicant applicant = applicantRepository.findById(request.getApplicantId())
                .orElseThrow(() -> new IllegalArgumentException("Applicant not found"));
        Beneficiary beneficiary = beneficiaryRepository.findById(request.getBeneficiaryId())
                .orElseThrow(() -> new IllegalArgumentException("Beneficiary not found"));
        IssuingBank issuingBank = issuingBankRepository.findById(request.getIssuingBankId())
                .orElseThrow(() -> new IllegalArgumentException("IssuingBank not found"));

        Guarantee guarantee = new Guarantee(
                request.getReference(),
                GuaranteeType.valueOf(request.getType()),
                request.getAmount(),
                request.getCurrency(),
                request.getIssueDate(),
                request.getExpiryDate(),
                GuaranteeStatus.DRAFT,
                applicant,
                beneficiary,
                issuingBank
        );

        Guarantee saved = guaranteeRepository.save(guarantee);
        GuaranteeDTO dto = toDTO(saved);
        publishChange(saved, "CREATED");
        return dto;
    }

    @CacheEvict(value = "metrics", allEntries = true)
    public GuaranteeDTO update(Long id, CreateGuaranteeRequest request) {
        Guarantee guarantee = guaranteeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Guarantee not found with id: " + id));

        guarantee.setReference(request.getReference());
        guarantee.setType(GuaranteeType.valueOf(request.getType()));
        guarantee.setAmount(request.getAmount());
        guarantee.setCurrency(request.getCurrency());
        guarantee.setIssueDate(request.getIssueDate());
        guarantee.setExpiryDate(request.getExpiryDate());

        Applicant applicant = applicantRepository.findById(request.getApplicantId())
                .orElseThrow(() -> new IllegalArgumentException("Applicant not found"));
        Beneficiary beneficiary = beneficiaryRepository.findById(request.getBeneficiaryId())
                .orElseThrow(() -> new IllegalArgumentException("Beneficiary not found"));
        IssuingBank issuingBank = issuingBankRepository.findById(request.getIssuingBankId())
                .orElseThrow(() -> new IllegalArgumentException("IssuingBank not found"));

        guarantee.setApplicant(applicant);
        guarantee.setBeneficiary(beneficiary);
        guarantee.setIssuingBank(issuingBank);

        Guarantee updated = guaranteeRepository.save(guarantee);
        GuaranteeDTO dto = toDTO(updated);
        publishChange(updated, "UPDATED");
        return dto;
    }

    @CacheEvict(value = "metrics", allEntries = true)
    public void delete(Long id) {
        if (!guaranteeRepository.existsById(id)) {
            throw new IllegalArgumentException("Guarantee not found with id: " + id);
        }
        Guarantee guarantee = guaranteeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Guarantee not found with id: " + id));
        guaranteeRepository.deleteById(id);
        publishChange(guarantee, "DELETED");
    }

    @CacheEvict(value = "metrics", allEntries = true)
    public GuaranteeDTO issue(Long id) {
        Guarantee guarantee = guaranteeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Guarantee not found with id: " + id));

        if (guarantee.getStatus() != GuaranteeStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT guarantees can be issued. Current status: " + guarantee.getStatus());
        }

        guarantee.setStatus(GuaranteeStatus.ISSUED);
        Guarantee updated = guaranteeRepository.save(guarantee);
        GuaranteeDTO dto = toDTO(updated);
        publishChange(updated, "ISSUED");
        return dto;
    }

    @CacheEvict(value = "metrics", allEntries = true)
    public GuaranteeDTO addAmendment(Long guaranteeId, BigDecimal newAmount, LocalDate newExpiryDate, String description) {
        Guarantee guarantee = guaranteeRepository.findById(guaranteeId)
                .orElseThrow(() -> new IllegalArgumentException("Guarantee not found with id: " + guaranteeId));

        if (guarantee.getStatus() != GuaranteeStatus.ISSUED && guarantee.getStatus() != GuaranteeStatus.AMENDED) {
            throw new IllegalStateException("Only ISSUED or AMENDED guarantees can have amendments. Current status: " + guarantee.getStatus());
        }

        Amendment amendment = new Amendment();
        amendment.setAmendmentDate(LocalDate.now());
        amendment.setDescription(description);
        amendment.setNewAmount(newAmount);
        amendment.setNewExpiryDate(newExpiryDate);
        amendment.setGuarantee(guarantee);

        amendmentRepository.save(amendment);
        guarantee.getAmendments().add(amendment);
        guarantee.setStatus(GuaranteeStatus.AMENDED);
        guarantee.setAmount(newAmount);
        guarantee.setExpiryDate(newExpiryDate);

        Guarantee updated = guaranteeRepository.save(guarantee);
        GuaranteeDTO dto = toDTO(updated);
        publishChange(updated, "AMENDED");
        return dto;
    }

    @CacheEvict(value = "metrics", allEntries = true)
    public GuaranteeDTO addClaim(Long guaranteeId, BigDecimal claimedAmount, String reason) {
        Guarantee guarantee = guaranteeRepository.findById(guaranteeId)
                .orElseThrow(() -> new IllegalArgumentException("Guarantee not found with id: " + guaranteeId));

        if (guarantee.getStatus() != GuaranteeStatus.ISSUED && guarantee.getStatus() != GuaranteeStatus.AMENDED) {
            throw new IllegalStateException("Only ISSUED or AMENDED guarantees can have claims. Current status: " + guarantee.getStatus());
        }

        Claim claim = new Claim();
        claim.setClaimDate(LocalDate.now());
        claim.setClaimedAmount(claimedAmount);
        claim.setStatus(ClaimStatus.SUBMITTED);
        claim.setReason(reason);
        claim.setGuarantee(guarantee);

        claimRepository.save(claim);
        guarantee.getClaims().add(claim);
        guarantee.setStatus(GuaranteeStatus.CLAIMED);

        Guarantee updated = guaranteeRepository.save(guarantee);
        GuaranteeDTO dto = toDTO(updated);
        publishChange(updated, "CLAIMED");
        return dto;
    }

    public List<ClaimDTO> listClaims(Long guaranteeId) {
        Guarantee guarantee = guaranteeRepository.findById(guaranteeId)
                .orElseThrow(() -> new IllegalArgumentException("Guarantee not found with id: " + guaranteeId));

        return guarantee.getClaims().stream()
                .map(claim -> new ClaimDTO(
                        claim.getId(),
                        claim.getClaimDate(),
                        claim.getClaimedAmount(),
                        claim.getStatus().name(),
                        claim.getReason()
                ))
                .collect(Collectors.toList());
    }

    private void publishChange(Guarantee guarantee, String action) {
        guaranteeEventService.publish(
                guarantee.getId(),
                action,
                guarantee.getReference(),
                guarantee.getStatus().name()
        );
    }

    private GuaranteeDTO toDTO(Guarantee guarantee) {
        ApplicantDTO applicantDTO = new ApplicantDTO(
                guarantee.getApplicant().getId(),
                guarantee.getApplicant().getFirstName(),
                guarantee.getApplicant().getLastName(),
                guarantee.getApplicant().getTaxId(),
                guarantee.getApplicant().getEmail(),
                guarantee.getApplicant().getPhone(),
                guarantee.getApplicant().getAddress(),
                guarantee.getApplicant().getCountry()
        );

        BeneficiaryDTO beneficiaryDTO = new BeneficiaryDTO(
                guarantee.getBeneficiary().getId(),
                guarantee.getBeneficiary().getFirstName(),
                guarantee.getBeneficiary().getLastName(),
                guarantee.getBeneficiary().getTaxId(),
                guarantee.getBeneficiary().getEmail(),
                guarantee.getBeneficiary().getPhone(),
                guarantee.getBeneficiary().getAddress(),
                guarantee.getBeneficiary().getCountry()
        );

        IssuingBankDTO issuingBankDTO = new IssuingBankDTO(
                guarantee.getIssuingBank().getId(),
                guarantee.getIssuingBank().getName(),
                guarantee.getIssuingBank().getBic(),
                guarantee.getIssuingBank().getCountry()
        );

        List<AmendmentDTO> amendmentDTOs = guarantee.getAmendments().stream()
                .map(amendment -> new AmendmentDTO(
                        amendment.getId(),
                        amendment.getAmendmentDate(),
                        amendment.getDescription(),
                        amendment.getNewAmount(),
                        amendment.getNewExpiryDate()
                ))
                .collect(Collectors.toList());

        List<ClaimDTO> claimDTOs = guarantee.getClaims().stream()
                .map(claim -> new ClaimDTO(
                        claim.getId(),
                        claim.getClaimDate(),
                        claim.getClaimedAmount(),
                        claim.getStatus().name(),
                        claim.getReason()
                ))
                .collect(Collectors.toList());

        return new GuaranteeDTO(
                guarantee.getId(),
                guarantee.getReference(),
                guarantee.getType().name(),
                guarantee.getAmount(),
                guarantee.getCurrency(),
                guarantee.getIssueDate(),
                guarantee.getExpiryDate(),
                guarantee.getStatus().name(),
                applicantDTO,
                beneficiaryDTO,
                issuingBankDTO,
                amendmentDTOs,
                claimDTOs
        );
    }
}
