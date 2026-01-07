package com.eazybytes.cards.dto;

import com.eazybytes.cards.constants.CardsConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;

@Schema(
        name = "Cards",
        description = "Schema to hold card details"
)
@Data
public class CardsDto
{
    @Schema(
            description = "Mobile Number of the user", example = "4354437687"
    )
    @NotEmpty(message = "Mobile number cannot be empty")
    @Pattern(regexp = "^[0-9]{10}$", message = "Invalid mobile number")
    private String mobileNumber;

    @Schema(
            description = "Card Number of the user", example = "3454433243"
    )
    @NotEmpty(message = "Card number cannot be empty")
    @Pattern(regexp = "^[0-9]{12}$", message = "Card NUmber must be 12 digits")
    private String cardNumber;

    @Schema(
            description = "Card Type of the card", example = CardsConstants.CREDIT_CARD
    )
    @NotEmpty(message = "Card type cannot be empty")
    private String cardType;

    @Schema(
            description = "Total Limit of the card", example = "1000000"
    )
    @Positive(message = "Total limit cannot be less than zero.")
    private int totalLimit;

    @Schema(
            description = "Amount Used by the user", example = "78345"
    )
    @PositiveOrZero(message = "Total amount used should be equal or greater than zero")
    private int amountUsed;

    @Schema(
            description = "Available amount of the card", example = "80800"
    )
    @PositiveOrZero(message = "Available amount cannot be less than zero.")
    private int availableAmount;
}
