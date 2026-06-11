package com.example.guarantees.service;

import com.example.guarantees.domain.Applicant;
import com.example.guarantees.dto.ApplicantDTO;
import com.example.guarantees.repository.ApplicantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ApplicantService {
    private final ApplicantRepository applicantRepository;

    public ApplicantService(ApplicantRepository applicantRepository) {
        this.applicantRepository = applicantRepository;
    }

    public List<ApplicantDTO> getAllApplicants() {
        return applicantRepository.findAll().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    private ApplicantDTO toDTO(Applicant applicant) {
        ApplicantDTO dto = new ApplicantDTO();
        dto.setId(applicant.getId());
        dto.setFirstName(applicant.getFirstName());
        dto.setLastName(applicant.getLastName());
        dto.setTaxId(applicant.getTaxId());
        dto.setEmail(applicant.getEmail());
        dto.setPhone(applicant.getPhone());
        return dto;
    }
}
