package com.adoptr.adoptr.adoption.service.impl;

import com.adoptr.adoptr.adoption.dto.request.AdoptionDTOin;
import com.adoptr.adoptr.adoption.dto.request.AdoptionFilterDTO;
import com.adoptr.adoptr.adoption.dto.request.AdoptionStatusDTOin;
import com.adoptr.adoptr.adoption.dto.response.AdoptionDTO;
import com.adoptr.adoptr.adoption.dto.response.AdoptionSummaryDTO;
import com.adoptr.adoptr.adoption.dto.response.OwnerDTO;
import com.adoptr.adoptr.adoption.error.AdoptionError;
import com.adoptr.adoptr.adoption.mapper.AdoptionMapper;
import com.adoptr.adoptr.adoption.model.AdoptionPublication;
import com.adoptr.adoptr.adoption.model.AdoptionStatus;
import com.adoptr.adoptr.adoption.model.Species;
import com.adoptr.adoptr.adoption.repository.AdoptionPublicationRepository;
import com.adoptr.adoptr.adoption.service.AdoptionService;
import com.adoptr.adoptr.adoption.spec.AdoptionSpec;
import com.adoptr.adoptr.location.error.LocationError;
import com.adoptr.adoptr.location.model.Locality;
import com.adoptr.adoptr.location.repository.LocalityRepository;
import com.adoptr.adoptr.profile.error.ProfileError;
import com.adoptr.adoptr.profile.repository.ProfileRepository;
import com.adoptr.adoptr.publication.model.PublicationType;
import com.adoptr.adoptr.shared.dto.PageDTO;
import com.adoptr.adoptr.shared.exception.custom.BadRequestException;
import com.adoptr.adoptr.shared.exception.custom.ForbiddenException;
import com.adoptr.adoptr.shared.exception.custom.NotFoundException;
import com.adoptr.adoptr.shared.exception.error.CommonError;
import com.adoptr.adoptr.shared.util.MembershipSupport;
import com.adoptr.adoptr.shared.util.Paging;
import com.adoptr.adoptr.user.repository.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdoptionServiceImpl implements AdoptionService {

    static final Sort NEWEST_FIRST = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
    private static final int MAX_AGE_YEARS = 30;

    private final MembershipSupport membershipSupport;
    private final AdoptionPublicationRepository adoptionRepository;
    private final ProfileRepository profileRepository;
    private final LocalityRepository localityRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    public AdoptionServiceImpl(MembershipSupport membershipSupport,
                               AdoptionPublicationRepository adoptionRepository,
                               ProfileRepository profileRepository,
                               LocalityRepository localityRepository,
                               UserRepository userRepository,
                               Clock clock) {
        this.membershipSupport = membershipSupport;
        this.adoptionRepository = adoptionRepository;
        this.profileRepository = profileRepository;
        this.localityRepository = localityRepository;
        this.userRepository = userRepository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public AdoptionDTO create(AdoptionDTOin request) {
        Long userId = membershipSupport.getUserId();
        if (!profileRepository.existsByUserId(userId)) {
            throw new ForbiddenException(ProfileError.REQUIRED);
        }
        Locality locality = validate(request);

        LocalDateTime now = LocalDateTime.now(clock);
        AdoptionPublication publication = new AdoptionPublication();
        publication.setType(PublicationType.ADOPTION);
        publication.setAuthor(userRepository.getReferenceById(userId));
        publication.setStatus(AdoptionStatus.AVAILABLE);
        publication.setCreatedAt(now);
        publication.setUpdatedAt(now);
        applyForm(publication, request, locality);
        return toDto(adoptionRepository.save(publication), true);
    }

    @Override
    @Transactional(readOnly = true)
    public AdoptionDTO get(Long id) {
        AdoptionPublication publication = findActive(id);
        boolean viewerIsOwner = membershipSupport.findUserId()
                .map(userId -> userId.equals(publication.getAuthor().getId()))
                .orElse(false);
        return toDto(publication, viewerIsOwner);
    }

    @Override
    @Transactional
    public AdoptionDTO update(Long id, AdoptionDTOin request) {
        AdoptionPublication publication = findOwned(id);
        Locality locality = validate(request);
        applyForm(publication, request, locality);
        publication.setUpdatedAt(LocalDateTime.now(clock));
        return toDto(adoptionRepository.save(publication), true);
    }

    @Override
    @Transactional
    public AdoptionDTO changeStatus(Long id, AdoptionStatusDTOin request) {
        AdoptionPublication publication = findOwned(id);
        if (publication.getStatus() != request.getStatus()) {
            publication.setStatus(request.getStatus());
            publication.setUpdatedAt(LocalDateTime.now(clock));
            publication = adoptionRepository.save(publication);
        }
        return toDto(publication, true);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        AdoptionPublication publication = findOwned(id);
        publication.setDeletedAt(LocalDateTime.now(clock));
        adoptionRepository.save(publication);
    }

    @Override
    @Transactional(readOnly = true)
    public PageDTO<AdoptionSummaryDTO> mine(int page, int pageSize) {
        Long userId = membershipSupport.getUserId();
        YearMonth current = YearMonth.now(clock);
        return PageDTO.from(
                adoptionRepository.findByAuthorIdAndDeletedAtIsNull(userId, Paging.pageable(page, pageSize, NEWEST_FIRST)),
                publication -> AdoptionMapper.toSummary(publication, current));
    }

    @Override
    @Transactional(readOnly = true)
    public PageDTO<AdoptionSummaryDTO> search(AdoptionFilterDTO filter, int page, int pageSize) {
        YearMonth current = YearMonth.now(clock);
        return PageDTO.from(
                adoptionRepository.findAll(AdoptionSpec.catalog(filter, current), Paging.pageable(page, pageSize, NEWEST_FIRST)),
                publication -> AdoptionMapper.toSummary(publication, current));
    }

    private AdoptionPublication findActive(Long id) {
        return adoptionRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException(AdoptionError.NOT_FOUND));
    }

    /** Primero 404 (no existe o está borrada), después 403 (es ajena). */
    private AdoptionPublication findOwned(Long id) {
        AdoptionPublication publication = findActive(id);
        if (!membershipSupport.getUserId().equals(publication.getAuthor().getId())) {
            throw new ForbiddenException(AdoptionError.NOT_OWNER);
        }
        return publication;
    }

    /** Reglas que cruzan campos; los obligatorios y largos ya los validó el controller. */
    private Locality validate(AdoptionDTOin request) {
        List<String> errors = new ArrayList<>();
        if (request.getSpecies() == Species.DOG && request.getSize() == null) {
            errors.add("size: obligatorio para perros");
        }
        if (request.getSpecies() == Species.CAT && request.getSize() != null) {
            errors.add("size: no corresponde para gatos");
        }
        YearMonth current = YearMonth.now(clock);
        YearMonth birth = request.getEstimatedBirthDate();
        if (birth.isAfter(current)) {
            errors.add("estimatedBirthDate: no puede ser futura");
        } else if (birth.isBefore(current.minusYears(MAX_AGE_YEARS))) {
            errors.add("estimatedBirthDate: no puede ser de hace más de " + MAX_AGE_YEARS + " años");
        }
        if (!errors.isEmpty()) {
            throw new BadRequestException(CommonError.VALIDATION_ERROR.getCode(),
                    CommonError.VALIDATION_ERROR.getMessage() + " (" + String.join("; ", errors) + ")");
        }
        return localityRepository.findById(request.getLocalityId())
                .orElseThrow(() -> new BadRequestException(LocationError.LOCALITY_NOT_FOUND));
    }

    private static void applyForm(AdoptionPublication publication, AdoptionDTOin request, Locality locality) {
        publication.setPetName(request.getPetName().trim());
        publication.setDescription(request.getDescription().trim());
        publication.setSpecies(request.getSpecies());
        publication.setSex(request.getSex());
        publication.setSize(request.getSize());
        publication.setEstimatedBirthDate(request.getEstimatedBirthDate().atDay(1));
        publication.setVaccinated(request.getVaccinated());
        publication.setDewormed(request.getDewormed());
        publication.setNeutered(request.getNeutered());
        publication.setLocality(locality);
    }

    private AdoptionDTO toDto(AdoptionPublication publication, boolean viewerIsOwner) {
        OwnerDTO owner = profileRepository.findByUserId(publication.getAuthor().getId())
                .map(AdoptionMapper::toOwner)
                .orElse(null);
        return AdoptionMapper.toDto(publication, YearMonth.now(clock), owner, viewerIsOwner);
    }
}
