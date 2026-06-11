package com.example.guarantees.service;

import com.example.guarantees.domain.IssuingBank;
import com.example.guarantees.dto.IssuingBankDTO;
import com.example.guarantees.repository.IssuingBankRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class IssuingBankService {
    private final IssuingBankRepository issuingBankRepository;

    public IssuingBankService(IssuingBankRepository issuingBankRepository) {
        this.issuingBankRepository = issuingBankRepository;
    }

    public List<IssuingBankDTO> getAllIssuingBanks() {
        return issuingBankRepository.findAll().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    private IssuingBankDTO toDTO(IssuingBank bank) {
        IssuingBankDTO dto = new IssuingBankDTO();
        dto.setId(bank.getId());
        dto.setCode(bank.getCode());
        dto.setName(bank.getName());
        dto.setCountry(bank.getCountry());
        return dto;
    }
}
