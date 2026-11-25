package mg.tsioryjonathan.ece_cost.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import mg.tsioryjonathan.ece_cost.entity.Resource;
import mg.tsioryjonathan.ece_cost.entity.ResourceType;
import mg.tsioryjonathan.ece_cost.entity.WorkItem;
import mg.tsioryjonathan.ece_cost.entity.WorkItemResource;

class PriceCalculatorTest {

  private static final BigDecimal K = new BigDecimal("1.35");

  private final PriceCalculator calculator = new PriceCalculator(K);

  @Test
  void lineCostIsQuantityTimesUnitPrice() {
    WorkItemResource line = line(resource("Ciment", "28000.00"), "7.000");

    assertThat(calculator.lineCost(line)).isEqualByComparingTo("196000");
  }

  @Test
  void lineCostKeepsEveryDecimalAndIsNeverRounded() {
    WorkItemResource line = line(resource("Ciment", "28000.55"), "0.333");

    assertThat(calculator.lineCost(line)).isEqualByComparingTo("9324.18315");
  }

  @Test
  void directCostIsTheSumOfTheLineCosts() {
    Resource cement = resource("Ciment", "28000.00");
    Resource sand = resource("Sable", "45000.00");
    WorkItem item = workItem("Mortier", "m3", line(cement, "2.000"), line(sand, "0.500"));

    assertThat(calculator.directCost(item)).isEqualByComparingTo("78500");
  }

  @Test
  void directCostOfAnItemWithoutLinesIsZero() {
    WorkItem item = workItem("Ouvrage vide", "m2");

    assertThat(calculator.directCost(item)).isEqualByComparingTo("0");
    assertThat(calculator.salePrice(calculator.directCost(item))).isZero();
  }

  @ParameterizedTest(name = "directCost {0} -> salePrice {1}")
  @CsvSource({
      "299500,    404325",
      "12350,     16673",
      "14750,     19913", // 19912.50, HALF_EVEN would give 19912
      "31450,     42458",
      "100,       135",
      "0,         0",
      "0.01,      0",
      "1000.30,   1350",
      "1000.40,   1351",
  })
  void salePriceIsDirectCostTimesKRoundedHalfUpToAWholeAriary(String directCost, long expected) {
    assertThat(calculator.salePrice(new BigDecimal(directCost))).isEqualTo(expected);
  }

  @Test
  void coefficientKComesFromTheConstructorAndIsNotHardCoded() {
    PriceCalculator doubling = new PriceCalculator(new BigDecimal("2"));

    assertThat(doubling.salePrice(new BigDecimal("100"))).isEqualTo(200);
  }

  @ParameterizedTest(name = "{0}: directCost {1} -> salePrice {2}")
  @CsvSource(delimiter = '|', value = {
      "Béton de fondation      | 299500 | 404325",
      "Enduit de façade        | 12350  | 16673",
      "Terrassement en fouille | 14750  | 19913",
      "Dallage béton 10 cm     | 31450  | 42458",
  })
  void seedWorkItemsHaveTheExpectedPrices(String name, String expectedDirectCost, long expectedSalePrice) {
    WorkItem item = seed().stream().filter(w -> w.getName().equals(name.trim())).findFirst().orElseThrow();

    BigDecimal directCost = calculator.directCost(item);

    assertThat(directCost).isEqualByComparingTo(expectedDirectCost);
    assertThat(calculator.salePrice(directCost)).isEqualTo(expectedSalePrice);
  }

  @Test
  void changingAResourcePriceChangesEveryWorkItemThatConsumesIt() {
    var items = seed();
    WorkItem foundation = byName(items, "Béton de fondation");
    WorkItem facade = byName(items, "Enduit de façade");
    WorkItem excavation = byName(items, "Terrassement en fouille");
    Resource cement = foundation.getLines().stream()
        .map(WorkItemResource::getResource)
        .filter(r -> r.getName().equals("Ciment"))
        .findFirst()
        .orElseThrow();

    long foundationBefore = salePriceOf(foundation);
    long facadeBefore = salePriceOf(facade);
    long excavationBefore = salePriceOf(excavation);

    cement.setUnitPrice(new BigDecimal("30000.00"));

    assertThat(salePriceOf(foundation)).isEqualTo(foundationBefore + 18900);
    assertThat(salePriceOf(foundation)).isEqualTo(423225);
    assertThat(calculator.directCost(facade)).isEqualByComparingTo("12850");
    assertThat(salePriceOf(facade)).isEqualTo(17348).isGreaterThan(facadeBefore);
    assertThat(salePriceOf(excavation)).isEqualTo(excavationBefore);
  }

  @Test
  void addingALineRaisesTheDirectCostByExactlyThatLine() {
    WorkItem foundation = byName(seed(), "Béton de fondation");
    Resource laborer = resource("Manœuvre", "2500.00");

    foundation.addLine(line(laborer, "0.400"));

    assertThat(calculator.directCost(foundation)).isEqualByComparingTo("300500");
    assertThat(calculator.salePrice(calculator.directCost(foundation))).isEqualTo(405675);
  }

  private long salePriceOf(WorkItem item) {
    return calculator.salePrice(calculator.directCost(item));
  }

  private static WorkItem byName(List<WorkItem> items, String name) {
    return items.stream().filter(w -> w.getName().equals(name)).findFirst().orElseThrow();
  }

  private static List<WorkItem> seed() {
    Resource cement = resource("Ciment", "28000.00");
    Resource sand = resource("Sable", "45000.00");
    Resource gravel = resource("Gravier", "60000.00");
    Resource mason = resource("Maçon", "5000.00");
    Resource laborer = resource("Manœuvre", "2500.00");
    Resource mixer = resource("Bétonnière", "12000.00");
    Resource excavator = resource("Pelle hydraulique", "90000.00");

    return List.of(
        workItem("Béton de fondation", "m3",
            line(cement, "7.000"), line(sand, "0.500"), line(gravel, "0.800"), line(mason, "3.000"), line(mixer, "1.500")),
        workItem("Enduit de façade", "m2",
            line(cement, "0.250"), line(sand, "0.030"), line(mason, "0.600"), line(laborer, "0.400")),
        workItem("Terrassement en fouille", "m3",
            line(excavator, "0.150"), line(laborer, "0.500")),
        workItem("Dallage béton 10 cm", "m2",
            line(cement, "0.700"), line(sand, "0.050"), line(gravel, "0.080"), line(mason, "0.400"),
            line(laborer, "0.400"), line(mixer, "0.150")));
  }

  private static Resource resource(String name, String unitPrice) {
    Resource resource = new Resource();
    resource.setName(name);
    resource.setType(ResourceType.MATERIAL);
    resource.setUnit("unit");
    resource.setUnitPrice(new BigDecimal(unitPrice));
    return resource;
  }

  private static WorkItemResource line(Resource resource, String quantity) {
    WorkItemResource line = new WorkItemResource();
    line.setResource(resource);
    line.setQuantity(new BigDecimal(quantity));
    return line;
  }

  private static WorkItem workItem(String name, String unit, WorkItemResource... lines) {
    WorkItem item = new WorkItem();
    item.setName(name);
    item.setUnit(unit);
    for (WorkItemResource line : lines) {
      item.addLine(line);
    }
    return item;
  }
}
