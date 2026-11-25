package mg.tsioryjonathan.ece_cost.mapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import mg.tsioryjonathan.ece_cost.dto.WorkItemLineResponse;
import mg.tsioryjonathan.ece_cost.dto.WorkItemResponse;
import mg.tsioryjonathan.ece_cost.entity.Resource;
import mg.tsioryjonathan.ece_cost.entity.WorkItem;
import mg.tsioryjonathan.ece_cost.entity.WorkItemResource;
import mg.tsioryjonathan.ece_cost.service.PriceCalculator;
import mg.tsioryjonathan.ece_cost.util.Sorting;

@Component
@RequiredArgsConstructor
public class WorkItemMapper {
  private final PriceCalculator calculator;

  public WorkItemResponse toResponse(WorkItem item) {
    List<WorkItemLineResponse> lines = item.getLines().stream()
        .sorted(Comparator.comparing(line -> line.getResource().getName(), Sorting.FRENCH))
        .map(this::toLine)
        .toList();

    BigDecimal directCost = calculator.directCost(item);
    return new WorkItemResponse(
        item.getId(),
        item.getName(),
        item.getUnit(),
        toAriary(directCost),
        calculator.salePrice(directCost),
        lines);
  }

  private WorkItemLineResponse toLine(WorkItemResource line) {
    Resource resource = line.getResource();
    return new WorkItemLineResponse(
        resource.getId(),
        resource.getName(),
        resource.getType(),
        resource.getUnit(),
        resource.getUnitPrice(),
        line.getQuantity(),
        toAriary(calculator.lineCost(line)));
  }

  private BigDecimal toAriary(BigDecimal amount) {
    return amount.setScale(0, RoundingMode.HALF_UP);
  }
}
