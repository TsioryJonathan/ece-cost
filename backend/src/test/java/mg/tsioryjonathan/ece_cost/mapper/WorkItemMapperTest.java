package mg.tsioryjonathan.ece_cost.mapper;

import static mg.tsioryjonathan.ece_cost.TestData.line;
import static mg.tsioryjonathan.ece_cost.TestData.resource;
import static mg.tsioryjonathan.ece_cost.TestData.workItem;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import mg.tsioryjonathan.ece_cost.dto.WorkItemResponse;
import mg.tsioryjonathan.ece_cost.entity.Resource;
import mg.tsioryjonathan.ece_cost.entity.WorkItem;
import mg.tsioryjonathan.ece_cost.service.PriceCalculator;

class WorkItemMapperTest {

  private final WorkItemMapper mapper = new WorkItemMapper(new PriceCalculator(new BigDecimal("1.35")));

  @Test
  void copiesTheWorkItemFieldsAndComputesItsPrices() {
    Resource cement = resource(1L, "Ciment", "28000.00");
    Resource sand = resource(2L, "Sable", "45000.00");
    WorkItem item = workItem(10L, "Mortier", "m3", line(cement, "2.000"), line(sand, "0.500"));

    WorkItemResponse response = mapper.toResponse(item);

    assertThat(response.id()).isEqualTo(10L);
    assertThat(response.name()).isEqualTo("Mortier");
    assertThat(response.unit()).isEqualTo("m3");
    assertThat(response.directCost()).isEqualByComparingTo("78500");
    assertThat(response.salePrice()).isEqualTo(105975);
  }

  @Test
  void eachLineCarriesItsResourceAndItsCost() {
    Resource cement = resource(1L, "Ciment", "28000.00");
    WorkItem item = workItem(10L, "Mortier", "m3", line(cement, "2.000"));

    var line = mapper.toResponse(item).lines().get(0);

    assertThat(line.resourceId()).isEqualTo(1L);
    assertThat(line.resourceName()).isEqualTo("Ciment");
    assertThat(line.unitPrice()).isEqualByComparingTo("28000");
    assertThat(line.quantity()).isEqualByComparingTo("2");
    assertThat(line.lineCost()).isEqualByComparingTo("56000");
  }

  @Test
  void linesAreSortedByResourceNameInFrenchOrder() {
    WorkItem item = workItem(10L, "Mortier", "m3",
        line(resource(1L, "Sable", "45000.00"), "1"),
        line(resource(2L, "Bétonnière", "12000.00"), "1"),
        line(resource(3L, "Maçon", "5000.00"), "1"),
        line(resource(4L, "Ciment", "28000.00"), "1"));

    var names = mapper.toResponse(item).lines().stream().map(l -> l.resourceName()).toList();

    assertThat(names).containsExactly("Bétonnière", "Ciment", "Maçon", "Sable");
  }

  @Test
  void eachAmountIsRoundedFromItsExactValue() {
    // each line: 0.7 x 1 = 0.7 -> 1
    // direct cost: 1.4 -> 1 (not 1 + 1 = 2)
    // sale price: 1.4 x 1.35 = 1.89 -> 2 (not 1 x 1.35 = 1.35 -> 1)
    WorkItem item = workItem(10L, "Test", "m2",
        line(resource(1L, "Ciment", "1.00"), "0.7000"),
        line(resource(2L, "Sable", "1.00"), "0.7000"));

    WorkItemResponse response = mapper.toResponse(item);

    assertThat(response.lines()).allSatisfy(line -> assertThat(line.lineCost()).isEqualByComparingTo("1"));
    assertThat(response.directCost()).isEqualByComparingTo("1");
    assertThat(response.salePrice()).isEqualTo(2);
  }

  @Test
  void aWorkItemWithoutLinesCostsNothing() {
    WorkItemResponse response = mapper.toResponse(workItem(10L, "Ouvrage vide", "m2"));

    assertThat(response.lines()).isEmpty();
    assertThat(response.directCost()).isEqualByComparingTo("0");
    assertThat(response.salePrice()).isZero();
  }
}
