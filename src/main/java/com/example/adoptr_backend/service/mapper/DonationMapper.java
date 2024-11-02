package com.example.adoptr_backend.service.mapper;


import com.example.adoptr_backend.model.Donation;
import com.example.adoptr_backend.service.dto.request.DonationDTOin;
import com.example.adoptr_backend.service.dto.response.DonationDTO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DonationMapper extends EntityMapper<DonationDTO, Donation>{
    DonationMapper MAPPER = Mappers.getMapper(DonationMapper.class);
    Donation toEntity(DonationDTOin dto);
}
