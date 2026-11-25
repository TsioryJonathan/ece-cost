package mg.tsioryjonathan.ece_cost.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import mg.tsioryjonathan.ece_cost.dto.ResourceResponse;
import mg.tsioryjonathan.ece_cost.dto.UpdateUnitPriceRequest;
import mg.tsioryjonathan.ece_cost.entity.Resource;
import mg.tsioryjonathan.ece_cost.exception.NotFoundException;
import mg.tsioryjonathan.ece_cost.mapper.ResourceMapper;
import mg.tsioryjonathan.ece_cost.repository.ResourceRepository;
import mg.tsioryjonathan.ece_cost.util.Sorting;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResourceService {
  private final ResourceRepository repository;
  private final ResourceMapper mapper;

  public List<ResourceResponse> findAllResources() {
    return repository.findAll().stream()
        .sorted(Comparator.comparing(Resource::getName, Sorting.FRENCH))
        .map(mapper::toResponse)
        .toList();
  }

  @Transactional
  public ResourceResponse updateUnitPrice(Long resourceId, UpdateUnitPriceRequest request) {
    Resource resource = repository.findById(resourceId)
        .orElseThrow(() -> new NotFoundException("Resource " + resourceId + " not found"));
    resource.setUnitPrice(request.unitPrice());
    return mapper.toResponse(resource);
  }
}
