package com.adoptr.adoptr.shared.mapper;

import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

public interface EntityMapper<D, I, E> {
    E toEntity(I dto);

    D toDto(E entity);

    List<D> toDto(List<E> entityList);

    List<E> toEntity(Iterable<D> dtoList);

    Iterable<D> toDto(Iterable<E> entityList);

    @Mapping(target = "id", ignore = true)
    void update(@MappingTarget E entity, E updateEntity);
}
