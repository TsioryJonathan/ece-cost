package mg.tsioryjonathan.ece_cost.mapper;

import org.springframework.stereotype.Component;

import mg.tsioryjonathan.ece_cost.dto.ResourceResponse;
import mg.tsioryjonathan.ece_cost.entity.Resource;

@Component
public class ResourceMapper {

  public ResourceResponse toResponse(Resource resource) {
    return new ResourceResponse(
        resource.getId(),
        resource.getName(),
        resource.getType(),
        resource.getUnit(),
        resource.getUnitPrice());
  }
}
