package com.sutulovai.jobops.service;

import com.sutulovai.jobops.dto.request.CreateCompanyRequest;
import com.sutulovai.jobops.dto.response.CompanyResponse;
import com.sutulovai.jobops.exception.NotFoundException;
import com.sutulovai.jobops.repository.CompanyRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public List<CompanyResponse> listCompanies(UUID userId) {
        return companyRepository.findByUserId(userId).stream().map(this::toResponse).toList();
    }

    public CompanyResponse getCompany(UUID id, UUID userId) {
        return companyRepository.findById(id, userId)
                .map(this::toResponse)
                .orElseThrow(() -> NotFoundException.forEntity("Company", id));
    }

    public CompanyResponse createCompany(UUID userId, CreateCompanyRequest req) {
        var row = new CompanyRepository.CompanyRow(
                null, userId,
                req.name(), req.website(), req.careersPageUrl(), req.linkedInUrl(),
                req.city(), req.country(),
                toArray(req.officeLocations()),
                req.remotePolicy(), req.industry(), req.companySize(), req.fundingStatus(),
                req.priorityTier() != null ? req.priorityTier() : "P2",
                req.englishLikelihood() != null ? req.englishLikelihood() : "UNCERTAIN",
                req.relocationFriendly() != null ? req.relocationFriendly() : "UNCERTAIN",
                req.visaSponsorship() != null ? req.visaSponsorship() : "UNCERTAIN",
                req.salaryPitchMin(), req.salaryPitchMax(),
                req.companyType(), req.fitReason(),
                toArray(req.likelyRoles()),
                req.recommendedStrategy(), req.notes(), req.sourceUrl(),
                req.status() != null ? req.status() : "WATCHLIST",
                null, null
        );
        return toResponse(companyRepository.save(row));
    }

    public CompanyResponse updateCompany(UUID id, UUID userId, CreateCompanyRequest req) {
        companyRepository.findById(id, userId)
                .orElseThrow(() -> NotFoundException.forEntity("Company", id));
        var row = new CompanyRepository.CompanyRow(
                id, userId,
                req.name(), req.website(), req.careersPageUrl(), req.linkedInUrl(),
                req.city(), req.country(),
                toArray(req.officeLocations()),
                req.remotePolicy(), req.industry(), req.companySize(), req.fundingStatus(),
                req.priorityTier() != null ? req.priorityTier() : "P2",
                req.englishLikelihood() != null ? req.englishLikelihood() : "UNCERTAIN",
                req.relocationFriendly() != null ? req.relocationFriendly() : "UNCERTAIN",
                req.visaSponsorship() != null ? req.visaSponsorship() : "UNCERTAIN",
                req.salaryPitchMin(), req.salaryPitchMax(),
                req.companyType(), req.fitReason(),
                toArray(req.likelyRoles()),
                req.recommendedStrategy(), req.notes(), req.sourceUrl(),
                req.status() != null ? req.status() : "WATCHLIST",
                null, null
        );
        return toResponse(companyRepository.save(row));
    }

    public void deleteCompany(UUID id, UUID userId) {
        companyRepository.findById(id, userId)
                .orElseThrow(() -> NotFoundException.forEntity("Company", id));
        companyRepository.delete(id, userId);
    }

    public CompanyResponse toResponse(CompanyRepository.CompanyRow c) {
        return new CompanyResponse(
                c.id(), c.userId(), c.name(), c.website(), c.careersPageUrl(), c.linkedInUrl(),
                c.city(), c.country(),
                c.officeLocations() != null ? Arrays.asList(c.officeLocations()) : List.of(),
                c.remotePolicy(), c.industry(), c.companySize(), c.fundingStatus(),
                c.priorityTier(), c.englishLikelihood(), c.relocationFriendly(), c.visaSponsorship(),
                c.salaryPitchMin(), c.salaryPitchMax(),
                c.companyType(), c.fitReason(),
                c.likelyRoles() != null ? Arrays.asList(c.likelyRoles()) : List.of(),
                c.recommendedStrategy(), c.notes(), c.sourceUrl(), c.status(),
                c.createdAt(), c.updatedAt()
        );
    }

    private static String[] toArray(List<String> list) {
        return list != null ? list.toArray(new String[0]) : new String[0];
    }
}
