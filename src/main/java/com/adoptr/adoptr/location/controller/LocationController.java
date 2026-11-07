package com.adoptr.adoptr.location.controller;

import com.adoptr.adoptr.location.dto.response.LocalityDTO;
import com.adoptr.adoptr.location.dto.response.ProvinceDTO;
import com.adoptr.adoptr.location.service.LocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/provinces")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping("")
    public ResponseEntity<List<ProvinceDTO>> getProvinces() {
        return ResponseEntity.ok(locationService.getProvinces());
    }

    @GetMapping("/{provinceId}/localities")
    public ResponseEntity<List<LocalityDTO>> searchLocalities(@PathVariable Long provinceId,
                                                              @RequestParam(name = "q", required = false) String query) {
        return ResponseEntity.ok(locationService.searchLocalities(provinceId, query));
    }
}
