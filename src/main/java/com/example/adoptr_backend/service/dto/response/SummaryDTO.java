package com.example.adoptr_backend.service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SummaryDTO {
    private Long publicationsCount;
    private Long profilesCount;
    private Long postCount;
    private Long adoptionPublicationsCount;
    private Long adoptedPetsCount;
    private Long servicePublicationsCount;
    private Long lostPublicationsCount;
    private Long donationCount;
}
