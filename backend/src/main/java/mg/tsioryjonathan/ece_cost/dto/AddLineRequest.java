package mg.tsioryjonathan.ece_cost.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AddLineRequest(
    @NotNull
    @Positive
    Long resourceId,

    @NotNull
    @DecimalMin(value = "0", inclusive = false)
    @Digits(integer = 8, fraction = 4)
    BigDecimal quantity) {
}
