package com.eazybytes.cards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Schema(name = "Response", description = "Representing Response body")
@Data@AllArgsConstructor
public class ResponseDto
{
    @Schema(description = "Status code of the response", example = "200")
    private String statusCode;
    @Schema(description = "Status message of the response", example = "Request processed successfully")
    private String statusMessage;
}
