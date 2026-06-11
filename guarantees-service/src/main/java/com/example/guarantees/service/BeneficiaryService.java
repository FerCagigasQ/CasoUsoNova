package com.example.guarantees.service;

import com.example.guarantees.domain.Beneficiary;
import com.example.guarantees.dto.BeneficiaryDTO;
import com.example.guarantees.repository.BeneficiaryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class BeneficiaryService {
    private final BeneficiaryRepository beneficiaryRepository;

    public BeneficiaryService(BeneficiaryRepository beneficiaryRepository) {
        this.beneficiaryRepository = beneficiaryRepository;
    }

    public List<BeneficiaryDTO> getAllBeneficiaries() {
        return beneficiaryRepository.findAll().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    private BeneficiaryDTO toDTO(Beneficiary beneficiary) {
        BeneficiaryDTO dto = new BeneficiaryDTO();
        dto.setId(beneficiary.getId());
        dto.setFirstName(beneficiary.getFirstName());
        dto.setLastName(beneficiary.getLastName());
        dto.setTaxId(beneficiary.getTaxId());
        dto.setEmail(beneficiary.getEmail());
        dto.setPhone(beneficiary.getPhone());
        return dto;
    }
}
