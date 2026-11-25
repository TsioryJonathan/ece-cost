package mg.tsioryjonathan.ece_cost.dto;

import java.math.BigDecimal;
import java.util.List;

public record WorkItemResponse(
    Long id,
    String name,
    String unit,
    BigDecimal directCost,
    long salePrice,
    List<WorkItemLineResponse> lines) {
}
