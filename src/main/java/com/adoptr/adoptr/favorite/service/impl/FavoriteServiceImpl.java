package com.adoptr.adoptr.favorite.service.impl;

import com.adoptr.adoptr.adoption.dto.response.AdoptionSummaryDTO;
import com.adoptr.adoptr.adoption.model.AdoptionPublication;
import com.adoptr.adoptr.adoption.model.Species;
import com.adoptr.adoptr.adoption.repository.AdoptionPublicationRepository;
import com.adoptr.adoptr.adoption.service.impl.AdoptionSummaries;
import com.adoptr.adoptr.favorite.dto.response.FavoriteIdsDTO;
import com.adoptr.adoptr.favorite.error.FavoriteError;
import com.adoptr.adoptr.favorite.model.Favorite;
import com.adoptr.adoptr.favorite.repository.FavoriteRepository;
import com.adoptr.adoptr.favorite.service.FavoriteService;
import com.adoptr.adoptr.publication.error.PublicationError;
import com.adoptr.adoptr.shared.dto.PageDTO;
import com.adoptr.adoptr.shared.exception.custom.ForbiddenException;
import com.adoptr.adoptr.shared.exception.custom.NotFoundException;
import com.adoptr.adoptr.shared.util.MembershipSupport;
import com.adoptr.adoptr.shared.util.Paging;
import com.adoptr.adoptr.user.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    private final MembershipSupport membershipSupport;
    private final FavoriteRepository favoriteRepository;
    private final AdoptionPublicationRepository adoptionRepository;
    private final UserRepository userRepository;
    private final AdoptionSummaries summaries;
    private final Clock clock;

    public FavoriteServiceImpl(MembershipSupport membershipSupport,
                               FavoriteRepository favoriteRepository,
                               AdoptionPublicationRepository adoptionRepository,
                               UserRepository userRepository,
                               AdoptionSummaries summaries,
                               Clock clock) {
        this.membershipSupport = membershipSupport;
        this.favoriteRepository = favoriteRepository;
        this.adoptionRepository = adoptionRepository;
        this.userRepository = userRepository;
        this.summaries = summaries;
        this.clock = clock;
    }

    @Override
    public void save(Long publicationId) {
        Long userId = membershipSupport.getUserId();
        AdoptionPublication publication = adoptionRepository.findByIdAndDeletedAtIsNull(publicationId)
                .orElseThrow(() -> new NotFoundException(PublicationError.NOT_FOUND));
        if (userId.equals(publication.getAuthor().getId())) {
            throw new ForbiddenException(FavoriteError.OWN_PUBLICATION);
        }
        if (favoriteRepository.existsByUserIdAndPublicationId(userId, publicationId)) {
            return;
        }
        Favorite favorite = new Favorite();
        favorite.setUser(userRepository.getReferenceById(userId));
        favorite.setPublication(publication);
        favorite.setCreatedAt(LocalDateTime.now(clock));
        try {
            favoriteRepository.saveAndFlush(favorite);
        } catch (DataIntegrityViolationException e) {
            return;
        }
    }

    @Override
    @Transactional
    public void unsave(Long publicationId) {
        favoriteRepository.deleteByUserIdAndPublicationId(membershipSupport.getUserId(), publicationId);
    }

    @Override
    @Transactional(readOnly = true)
    public PageDTO<AdoptionSummaryDTO> list(Species species, int page, int pageSize) {
        Long userId = membershipSupport.getUserId();
        return summaries.page(favoriteRepository.findAdoptions(userId, species,
                Paging.pageable(page, pageSize, Sort.unsorted())));
    }

    @Override
    @Transactional(readOnly = true)
    public FavoriteIdsDTO ids() {
        return new FavoriteIdsDTO(favoriteRepository.findPublicationIds(membershipSupport.getUserId()));
    }
}
