package com.adoptr.adoptr.location.loader;

import com.adoptr.adoptr.location.model.Locality;
import com.adoptr.adoptr.location.model.Province;
import com.adoptr.adoptr.shared.util.SearchText;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Convierte la copia de Georef en entidades. Calcula el nombre para mostrar y
 * el nombre normalizado para buscar
 */
public record GeorefCatalog(List<Province> provinces, List<Locality> localities) {

    public static GeorefCatalog from(GeorefSnapshot snapshot) {
        List<Province> provinces = snapshot.provinces().stream()
                .map(row -> {
                    Province province = new Province();
                    province.setId(Long.parseLong(row.id()));
                    province.setName(row.name());
                    return province;
                })
                .toList();
        Map<Long, Province> provincesById = provinces.stream()
                .collect(Collectors.toMap(Province::getId, Function.identity()));

        Map<String, Integer> nameCount = new HashMap<>();
        snapshot.localities().forEach(row -> nameCount.merge(nameKey(row), 1, Integer::sum));

        List<Locality> localities = snapshot.localities().stream()
                .map(row -> {
                    Province province = provincesById.get(Long.parseLong(row.provinceId()));
                    if (province == null) {
                        throw new IllegalStateException("Localidad " + row.id() + " con provincia inexistente " + row.provinceId());
                    }
                    boolean repeated = nameCount.get(nameKey(row)) > 1;
                    Locality locality = new Locality();
                    locality.setId(Long.parseLong(row.id()));
                    locality.setName(row.name());
                    locality.setDepartmentName(row.departmentName());
                    locality.setDisplayName(repeated ? row.name() + " (" + row.departmentName() + ")" : row.name());
                    locality.setSearchName(SearchText.normalize(row.name()));
                    locality.setProvince(province);
                    return locality;
                })
                .toList();
        return new GeorefCatalog(provinces, localities);
    }

    private static String nameKey(GeorefSnapshot.LocalityRow row) {
        return row.provinceId() + "|" + SearchText.normalize(row.name());
    }
}
