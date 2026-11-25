package mg.tsioryjonathan.ece_cost.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mg.tsioryjonathan.ece_cost.dto.ResourceResponse;
import mg.tsioryjonathan.ece_cost.dto.UpdateUnitPriceRequest;
import mg.tsioryjonathan.ece_cost.service.ResourceService;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceController {
  private final ResourceService service;

  @GetMapping
  public List<ResourceResponse> list() {
    return service.findAllResources();
  }

  @PatchMapping("/{resourceId}")
  public ResourceResponse updateUnitPrice(@PathVariable Long resourceId, @Valid @RequestBody UpdateUnitPriceRequest request) {
    return service.updateUnitPrice(resourceId, request);
  }
}
