package mg.tsioryjonathan.ece_cost.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

public record UpdateUnitPriceRequest(
    @NotNull
    @DecimalMin("0")
    @Digits(integer = 12, fraction = 2)
    BigDecimal unitPrice) {
}
