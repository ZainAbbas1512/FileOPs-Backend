package org.example.dto.request;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Setter
@Getter
public class DeleteFileRequest {
    @NotNull(message = "File ID is mandatory")
    private UUID id;
}