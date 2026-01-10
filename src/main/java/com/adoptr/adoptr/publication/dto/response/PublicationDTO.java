package com.adoptr.adoptr.publication.dto.response;

import com.adoptr.adoptr.publication.model.PublicationType;
import com.adoptr.adoptr.user.dto.response.UserDTO;
import lombok.Data;

@Data
public class PublicationDTO {
    private Long id;
    private String title;
    private PublicationType type;
    private UserDTO user;
}
