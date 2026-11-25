package mg.tsioryjonathan.ece_cost.service;

import static mg.tsioryjonathan.ece_cost.TestData.line;
import static mg.tsioryjonathan.ece_cost.TestData.resource;
import static mg.tsioryjonathan.ece_cost.TestData.workItem;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import mg.tsioryjonathan.ece_cost.dto.AddLineRequest;
import mg.tsioryjonathan.ece_cost.dto.WorkItemResponse;
import mg.tsioryjonathan.ece_cost.entity.Resource;
import mg.tsioryjonathan.ece_cost.entity.WorkItem;
import mg.tsioryjonathan.ece_cost.exception.ConflictException;
import mg.tsioryjonathan.ece_cost.exception.NotFoundException;
import mg.tsioryjonathan.ece_cost.mapper.WorkItemMapper;
import mg.tsioryjonathan.ece_cost.repository.ResourceRepository;
import mg.tsioryjonathan.ece_cost.repository.WorkItemRepository;

@ExtendWith(MockitoExtension.class)
class WorkItemServiceTest {

  @Mock
  WorkItemRepository workItems;
  @Mock
  ResourceRepository resources;

  WorkItemService service;

  @BeforeEach
  void setUp() {
    WorkItemMapper mapper = new WorkItemMapper(new PriceCalculator(new BigDecimal("1.35")));
    service = new WorkItemService(workItems, resources, mapper);
  }

  @Test
  void listsTheWorkItemsSortedById() {
    when(workItems.findAllWithLines()).thenReturn(List.of(
        workItem(3L, "Terrassement", "m3"),
        workItem(1L, "Béton", "m3"),
        workItem(2L, "Dallage", "m2")));

    var ids = service.findAllWorkItems().stream().map(WorkItemResponse::id).toList();

    assertThat(ids).containsExactly(1L, 2L, 3L);
  }

  @Test
  void addingALineReturnsTheWorkItemRecalculated() {
    Resource cement = resource(1L, "Ciment", "28000.00");
    Resource laborer = resource(5L, "Manœuvre", "2500.00");
    WorkItem item = workItem(10L, "Mortier", "m3", line(cement, "2.000"));
    when(workItems.findByIdWithLines(10L)).thenReturn(Optional.of(item));
    when(resources.findById(5L)).thenReturn(Optional.of(laborer));

    WorkItemResponse response = service.addLine(10L, new AddLineRequest(5L, new BigDecimal("0.4")));

    assertThat(item.getLines()).hasSize(2);
    assertThat(response.lines()).hasSize(2);
    assertThat(response.directCost()).isEqualByComparingTo("57000"); // 56000 + 0.4 x 2500
    assertThat(response.salePrice()).isEqualTo(76950);
  }

  @Test
  void addingALineToAnUnknownWorkItemIs404() {
    when(workItems.findByIdWithLines(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.addLine(99L, new AddLineRequest(1L, BigDecimal.ONE)))
        .isInstanceOf(NotFoundException.class);
    verifyNoInteractions(resources);
  }

  @Test
  void addingAnUnknownResourceIs404() {
    WorkItem item = workItem(10L, "Mortier", "m3");
    when(workItems.findByIdWithLines(10L)).thenReturn(Optional.of(item));
    when(resources.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.addLine(10L, new AddLineRequest(99L, BigDecimal.ONE)))
        .isInstanceOf(NotFoundException.class);
    assertThat(item.getLines()).isEmpty();
  }

  @Test
  void addingAResourceAlreadyInTheWorkItemIs409() {
    Resource cement = resource(1L, "Ciment", "28000.00");
    WorkItem item = workItem(10L, "Mortier", "m3", line(cement, "2.000"));
    when(workItems.findByIdWithLines(10L)).thenReturn(Optional.of(item));
    when(resources.findById(1L)).thenReturn(Optional.of(cement));

    assertThatThrownBy(() -> service.addLine(10L, new AddLineRequest(1L, BigDecimal.ONE)))
        .isInstanceOf(ConflictException.class);
    assertThat(item.getLines()).hasSize(1);
  }
}
