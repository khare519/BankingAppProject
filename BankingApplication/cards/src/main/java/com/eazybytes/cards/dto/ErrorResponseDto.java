package com.eazybytes.cards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Schema(name = "Error", description = "Representing Error Response body")
@Data@AllArgsConstructor
public class ErrorResponseDto
{
    @Schema(description = "Represents API path failed")
    private String apiPath;
    @Schema(description = "Represents HTTP error Status code")
    private HttpStatus errorCode;
    @Schema(description = "Represents error message")
    private String errorMessage;
    @Schema(description = "Represents error time")
    private LocalDateTime errorTime;
}
