package mg.tsioryjonathan.ece_cost.dto;

import java.math.BigDecimal;

import mg.tsioryjonathan.ece_cost.entity.ResourceType;

public record ResourceResponse(
    Long id,
    String name,
    ResourceType type,
    String unit,
    BigDecimal unitPrice) {
}
