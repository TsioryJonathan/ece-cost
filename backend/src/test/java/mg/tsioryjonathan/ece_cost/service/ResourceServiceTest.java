package mg.tsioryjonathan.ece_cost.service;

import static mg.tsioryjonathan.ece_cost.TestData.resource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import mg.tsioryjonathan.ece_cost.dto.ResourceResponse;
import mg.tsioryjonathan.ece_cost.dto.UpdateUnitPriceRequest;
import mg.tsioryjonathan.ece_cost.entity.Resource;
import mg.tsioryjonathan.ece_cost.exception.NotFoundException;
import mg.tsioryjonathan.ece_cost.mapper.ResourceMapper;
import mg.tsioryjonathan.ece_cost.repository.ResourceRepository;

@ExtendWith(MockitoExtension.class)
class ResourceServiceTest {

  @Mock
  ResourceRepository repository;

  ResourceService service;

  @BeforeEach
  void setUp() {
    service = new ResourceService(repository, new ResourceMapper());
  }

  @Test
  void listsTheResourcesSortedByNameInFrenchOrder() {
    when(repository.findAll()).thenReturn(List.of(
        resource(1L, "Sable", "45000.00"),
        resource(2L, "Ciment", "28000.00"),
        resource(3L, "Bétonnière", "12000.00")));

    var names = service.findAllResources().stream().map(ResourceResponse::name).toList();

    assertThat(names).containsExactly("Bétonnière", "Ciment", "Sable");
  }

  @Test
  void updatingTheUnitPriceChangesTheResourceAndReturnsIt() {
    Resource cement = resource(1L, "Ciment", "28000.00");
    when(repository.findById(1L)).thenReturn(Optional.of(cement));

    ResourceResponse response = service.updateUnitPrice(1L, new UpdateUnitPriceRequest(new BigDecimal("30000.00")));

    assertThat(cement.getUnitPrice()).isEqualByComparingTo("30000");
    assertThat(response.unitPrice()).isEqualByComparingTo("30000");
  }

  @Test
  void updatingAnUnknownResourceIs404() {
    when(repository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.updateUnitPrice(99L, new UpdateUnitPriceRequest(BigDecimal.TEN)))
        .isInstanceOf(NotFoundException.class);
  }
}
