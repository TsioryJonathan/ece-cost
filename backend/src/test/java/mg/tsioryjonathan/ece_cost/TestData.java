package mg.tsioryjonathan.ece_cost;

import java.math.BigDecimal;

import mg.tsioryjonathan.ece_cost.entity.Resource;
import mg.tsioryjonathan.ece_cost.entity.ResourceType;
import mg.tsioryjonathan.ece_cost.entity.WorkItem;
import mg.tsioryjonathan.ece_cost.entity.WorkItemResource;

public class TestData {

  public static Resource resource(Long id, String name, String unitPrice) {
    Resource resource = new Resource();
    resource.setId(id);
    resource.setName(name);
    resource.setType(ResourceType.MATERIAL);
    resource.setUnit("unit");
    resource.setUnitPrice(new BigDecimal(unitPrice));
    return resource;
  }

  public static WorkItemResource line(Resource resource, String quantity) {
    WorkItemResource line = new WorkItemResource();
    line.setResource(resource);
    line.setQuantity(new BigDecimal(quantity));
    return line;
  }

  public static WorkItem workItem(Long id, String name, String unit, WorkItemResource... lines) {
    WorkItem item = new WorkItem();
    item.setId(id);
    item.setName(name);
    item.setUnit(unit);
    for (WorkItemResource line : lines) {
      item.addLine(line);
    }
    return item;
  }
}
