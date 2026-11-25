package mg.tsioryjonathan.ece_cost.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import mg.tsioryjonathan.ece_cost.dto.AddLineRequest;
import mg.tsioryjonathan.ece_cost.dto.WorkItemResponse;
import mg.tsioryjonathan.ece_cost.entity.Resource;
import mg.tsioryjonathan.ece_cost.entity.WorkItem;
import mg.tsioryjonathan.ece_cost.entity.WorkItemResource;
import mg.tsioryjonathan.ece_cost.exception.ConflictException;
import mg.tsioryjonathan.ece_cost.exception.NotFoundException;
import mg.tsioryjonathan.ece_cost.mapper.WorkItemMapper;
import mg.tsioryjonathan.ece_cost.repository.ResourceRepository;
import mg.tsioryjonathan.ece_cost.repository.WorkItemRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkItemService {
  private final WorkItemRepository repository;
  private final ResourceRepository resourceRepository;
  private final WorkItemMapper mapper;

  public List<WorkItemResponse> findAllWorkItems() {
    return repository.findAllWithLines().stream()
        .sorted(Comparator.comparing(WorkItem::getId))
        .map(mapper::toResponse)
        .toList();
  }

  @Transactional
  public WorkItemResponse addLine(Long workItemId, AddLineRequest request) {
    WorkItem item = repository.findByIdWithLines(workItemId)
        .orElseThrow(() -> new NotFoundException("Work item " + workItemId + " not found"));
    Resource resource = resourceRepository.findById(request.resourceId())
        .orElseThrow(() -> new NotFoundException("Resource " + request.resourceId() + " not found"));

    boolean alreadyThere = item.getLines().stream().anyMatch(line -> line.getResource().getId().equals(resource.getId()));
    if (alreadyThere) {
      throw new ConflictException("Resource " + resource.getId() + " is already in work item " + workItemId);
    }

    WorkItemResource line = new WorkItemResource();
    line.setResource(resource);
    line.setQuantity(request.quantity());
    item.addLine(line);

    return mapper.toResponse(item);
  }
}
