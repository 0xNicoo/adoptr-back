package com.adoptr.adoptr.location.mapper;

import com.adoptr.adoptr.location.dto.response.LocalityDTO;
import com.adoptr.adoptr.location.dto.response.ProvinceDTO;
import com.adoptr.adoptr.location.model.Locality;
import com.adoptr.adoptr.location.model.Province;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LocationMapper {
    LocationMapper MAPPER = Mappers.getMapper(LocationMapper.class);

    ProvinceDTO toDto(Province province);

    List<ProvinceDTO> toProvinceDtos(List<Province> provinces);

    LocalityDTO toDto(Locality locality);

    List<LocalityDTO> toLocalityDtos(List<Locality> localities);
}
