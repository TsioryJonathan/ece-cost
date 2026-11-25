package mg.tsioryjonathan.ece_cost.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import mg.tsioryjonathan.ece_cost.entity.WorkItemResource;
import mg.tsioryjonathan.ece_cost.entity.WorkItem;

@Component
public class PriceCalculator {
  private final BigDecimal k;

  public PriceCalculator(@Value("${app.coefficient-k}") BigDecimal k) {
    this.k = k;
  }

  public BigDecimal coefficient() {
    return k;
  }

  public BigDecimal lineCost(WorkItemResource line) {
    return line.getResource().getUnitPrice().multiply(line.getQuantity());
  }

  public BigDecimal directCost(WorkItem item) {
    return item.getLines().stream()
        .map(this::lineCost)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  public long salePrice(BigDecimal directCost) {
    return directCost.multiply(k).setScale(0, RoundingMode.HALF_UP).longValueExact();
  }

}
