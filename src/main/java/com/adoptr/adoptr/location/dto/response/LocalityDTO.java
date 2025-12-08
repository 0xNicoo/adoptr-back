package com.adoptr.adoptr.location.dto.response;

import lombok.Data;

@Data
public class LocalityDTO {
    private Long id;
    private String name;
    private ProvinceDTO province;
}
