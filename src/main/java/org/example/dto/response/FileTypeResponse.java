package org.example.dto.response;


import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class FileTypeResponse {

    private UUID id;
    private String type;

}
