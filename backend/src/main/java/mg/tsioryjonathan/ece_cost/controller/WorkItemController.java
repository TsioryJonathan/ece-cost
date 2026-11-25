package mg.tsioryjonathan.ece_cost.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mg.tsioryjonathan.ece_cost.dto.AddLineRequest;
import mg.tsioryjonathan.ece_cost.dto.WorkItemResponse;
import mg.tsioryjonathan.ece_cost.service.WorkItemService;

@RestController
@RequestMapping("/api/work-items")
@RequiredArgsConstructor
public class WorkItemController {
  private final WorkItemService service;

  @GetMapping
  public List<WorkItemResponse> list() {
    return service.findAllWorkItems();
  }

  @PostMapping("/{workItemId}/resources")
  @ResponseStatus(HttpStatus.CREATED)
  public WorkItemResponse addLine(@PathVariable Long workItemId, @Valid @RequestBody AddLineRequest request) {
    return service.addLine(workItemId, request);
  }
}
