package mg.tsioryjonathan.ece_cost.dto;

import java.math.BigDecimal;

import mg.tsioryjonathan.ece_cost.entity.ResourceType;

public record WorkItemLineResponse(
    Long resourceId,
    String resourceName,
    ResourceType resourceType,
    String unit,
    BigDecimal unitPrice,
    BigDecimal quantity,
    BigDecimal lineCost) {
}
