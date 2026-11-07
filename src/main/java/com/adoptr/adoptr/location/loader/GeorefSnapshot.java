package com.adoptr.adoptr.location.loader;

import java.util.List;

/** Copia recortada del servicie Georef guardada en {@code data/georef/locations.json}. */
public record GeorefSnapshot(String source,
                             String license,
                             String downloadedAt,
                             List<ProvinceRow> provinces,
                             List<LocalityRow> localities) {

    public record ProvinceRow(String id, String name) {
    }

    public record LocalityRow(String id, String name, String provinceId, String departmentName) {
    }
}
