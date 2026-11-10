package com.adoptr.adoptr.profile.service.impl;

import com.adoptr.adoptr.location.error.LocationError;
import com.adoptr.adoptr.location.model.Locality;
import com.adoptr.adoptr.location.repository.LocalityRepository;
import com.adoptr.adoptr.profile.dto.request.ProfileDTOin;
import com.adoptr.adoptr.profile.dto.response.ProfileDTO;
import com.adoptr.adoptr.profile.dto.response.PublicProfileDTO;
import com.adoptr.adoptr.profile.error.ProfileError;
import com.adoptr.adoptr.profile.mapper.ProfileMapper;
import com.adoptr.adoptr.profile.model.OrganizationProfile;
import com.adoptr.adoptr.profile.model.PersonProfile;
import com.adoptr.adoptr.profile.model.Profile;
import com.adoptr.adoptr.profile.model.ProfilePhoto;
import com.adoptr.adoptr.profile.model.ProfileType;
import com.adoptr.adoptr.profile.repository.ProfilePhotoRepository;
import com.adoptr.adoptr.profile.repository.ProfileRepository;
import com.adoptr.adoptr.profile.service.ProfileService;
import com.adoptr.adoptr.shared.exception.custom.BadRequestException;
import com.adoptr.adoptr.shared.exception.custom.ConflictException;
import com.adoptr.adoptr.shared.exception.custom.NotFoundException;
import com.adoptr.adoptr.shared.exception.error.CommonError;
import com.adoptr.adoptr.shared.storage.ImageProcessor;
import com.adoptr.adoptr.shared.storage.ImageStorage;
import com.adoptr.adoptr.shared.storage.ProcessedImage;
import com.adoptr.adoptr.shared.util.MembershipSupport;
import com.adoptr.adoptr.user.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class ProfileServiceImpl implements ProfileService {

    private final MembershipSupport membershipSupport;
    private final ProfileRepository profileRepository;
    private final LocalityRepository localityRepository;
    private final UserRepository userRepository;
    private final ProfilePhotoRepository photoRepository;
    private final ImageProcessor imageProcessor;
    private final ImageStorage imageStorage;
    private final Clock clock;

    public ProfileServiceImpl(MembershipSupport membershipSupport,
                              ProfileRepository profileRepository,
                              LocalityRepository localityRepository,
                              UserRepository userRepository,
                              ProfilePhotoRepository photoRepository,
                              ImageProcessor imageProcessor,
                              ImageStorage imageStorage,
                              Clock clock) {
        this.membershipSupport = membershipSupport;
        this.profileRepository = profileRepository;
        this.localityRepository = localityRepository;
        this.userRepository = userRepository;
        this.photoRepository = photoRepository;
        this.imageProcessor = imageProcessor;
        this.imageStorage = imageStorage;
        this.clock = clock;
    }

    @Override
    public ProfileDTO get() {
        return toDto(findOwn());
    }

    @Override
    @Transactional(readOnly = true)
    public PublicProfileDTO getPublic(Long id) {
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ProfileError.NOT_FOUND));
        PublicProfileDTO dto = ProfileMapper.toPublicDto(profile);
        dto.setPhotoUrl(photoUrl(profile.getId()));
        return dto;
    }

    /** Sin transacción: no retiene una conexión de la base mientras procesa la imagen y la sube a S3. */
    @Override
    public ProfileDTO setPhoto(MultipartFile file) {
        Profile profile = findOwn();
        ProcessedImage image = imageProcessor.process(file);
        String key = "profiles/" + profile.getId() + "/" + UUID.randomUUID() + "." + image.extension();
        imageStorage.upload(key, image);

        ProfilePhoto photo = photoRepository.findByProfileId(profile.getId()).orElseGet(ProfilePhoto::new);
        String previousKey = photo.getS3Key();
        photo.setProfile(profile);
        photo.fillFrom(key, image, LocalDateTime.now(clock));
        try {
            photoRepository.saveAndFlush(photo);
        } catch (DataIntegrityViolationException e) {
            // Dos primeras subidas simultáneas: la restricción única de profile_id frena a una.
            imageStorage.delete(key);
            throw new ConflictException(ProfileError.PHOTO_CONFLICT);
        } catch (RuntimeException e) {
            imageStorage.delete(key);
            throw e;
        }
        if (previousKey != null) {
            imageStorage.delete(previousKey);
        }
        return toDto(profile);
    }

    @Override
    public void deletePhoto() {
        Profile profile = findOwn();
        ProfilePhoto photo = photoRepository.findByProfileId(profile.getId())
                .orElseThrow(() -> new NotFoundException(ProfileError.PHOTO_NOT_FOUND));
        photoRepository.delete(photo);
        imageStorage.delete(photo.getS3Key());
    }

    @Override
    public String photoUrl(Long profileId) {
        return photoRepository.findByProfileId(profileId)
                .map(photo -> imageStorage.publicUrl(photo.getS3Key()))
                .orElse(null);
    }

    private Profile findOwn() {
        return profileRepository.findByUserId(membershipSupport.getUserId())
                .orElseThrow(() -> new NotFoundException(ProfileError.NOT_FOUND));
    }

    private ProfileDTO toDto(Profile profile) {
        ProfileDTO dto = ProfileMapper.toDto(profile);
        dto.setPhotoUrl(photoUrl(profile.getId()));
        return dto;
    }

    @Override
    @Transactional
    public ProfileDTO create(ProfileDTOin request) {
        Long userId = membershipSupport.getUserId();
        Profile profile = buildProfile(request);

        if (profileRepository.existsByUserId(userId)) {
            throw new ConflictException(ProfileError.ALREADY_EXISTS);
        }
        Locality locality = localityRepository.findById(request.getLocalityId())
                .orElseThrow(() -> new BadRequestException(LocationError.LOCALITY_NOT_FOUND));

        profile.setUser(userRepository.getReferenceById(userId));
        profile.setLocality(locality);
        profile.setDescription(clean(request.getDescription()));
        try {
            return toDto(profileRepository.saveAndFlush(profile));
        } catch (DataIntegrityViolationException e) {
            // Dos altas simultáneas del mismo usuario: la restricción única de user_id gana.
            throw new ConflictException(ProfileError.ALREADY_EXISTS);
        }
    }

    private Profile buildProfile(ProfileDTOin request) {
        List<String> errors = new ArrayList<>();
        Profile profile;
        if (request.getProfileType() == ProfileType.PERSON) {
            PersonProfile person = new PersonProfile();
            person.setFirstName(required(request.getFirstName(), "firstName", errors));
            person.setLastName(required(request.getLastName(), "lastName", errors));
            if (request.getGenderType() == null) {
                errors.add("genderType: es obligatorio");
            }
            person.setGenderType(request.getGenderType());
            profile = person;
        } else {
            OrganizationProfile organization = new OrganizationProfile();
            organization.setOrganizationName(required(request.getOrganizationName(), "organizationName", errors));
            organization.setInstagram(clean(request.getInstagram()));
            organization.setFacebook(clean(request.getFacebook()));
            String website = clean(request.getWebsite());
            if (website != null && !isHttpUrl(website)) {
                errors.add("website: debe empezar con http:// o https://");
            }
            organization.setWebsite(website);
            profile = organization;
        }
        if (!errors.isEmpty()) {
            String detail = String.join("; ", errors.stream().sorted().toList());
            throw new BadRequestException(CommonError.VALIDATION_ERROR.getCode(),
                    CommonError.VALIDATION_ERROR.getMessage() + " (" + detail + ")");
        }
        return profile;
    }

    private static String required(String value, String field, List<String> errors) {
        String cleaned = clean(value);
        if (cleaned == null) {
            errors.add(field + ": es obligatorio");
        }
        return cleaned;
    }

    /** Recorta espacios para que un texto en blanco cuente como ausente. */
    private static String clean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private static boolean isHttpUrl(String value) {
        String lower = value.toLowerCase(Locale.ROOT);
        return (lower.startsWith("http://") && lower.length() > 7)
                || (lower.startsWith("https://") && lower.length() > 8);
    }
}
