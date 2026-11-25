package mg.tsioryjonathan.ece_cost.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

class GlobalExceptionHandlerTest {

  private MockMvc mvc;

  @BeforeEach
  void setUp() {
    mvc = MockMvcBuilders.standaloneSetup(new FakeController())
        .setControllerAdvice(new GlobalExceptionHandler())
        .build();
  }

  @Test
  void invalidBodyReturns400WithFieldErrorsInDetail() throws Exception {
    mvc.perform(post("/validate").contentType(MediaType.APPLICATION_JSON).content("{\"quantity\": -1}"))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.detail").value("quantity: must be greater than 0"));
  }

  @Test
  void malformedJsonReturns400() throws Exception {
    mvc.perform(post("/validate").contentType(MediaType.APPLICATION_JSON).content("{\"quantity\": \"abc\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
  }

  @Test
  void responseStatusExceptionKeepsStatusAndReason() throws Exception {
    mvc.perform(get("/not-found"))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.detail").value("Work item 9 not found"));
  }

  @Test
  void notFoundExceptionReturns404WithItsMessage() throws Exception {
    mvc.perform(get("/missing"))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.detail").value("Resource 9 not found"));
  }

  @Test
  void conflictExceptionReturns409WithItsMessage() throws Exception {
    mvc.perform(get("/already-there"))
        .andExpect(status().isConflict())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.detail").value("Resource 1 is already in work item 1"));
  }

  @Test
  void dataIntegrityViolationReturns409() throws Exception {
    mvc.perform(get("/duplicate"))
        .andExpect(status().isConflict())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.detail").value("The request conflicts with existing data."));
  }

  @Test
  void unexpectedErrorReturns500WithoutLeakingMessage() throws Exception {
    mvc.perform(get("/boom"))
        .andExpect(status().isInternalServerError())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.detail").value("Unexpected server error."));
  }

  record Body(@NotNull @Positive BigDecimal quantity) {}

  @RestController
  static class FakeController {

    @PostMapping("/validate")
    void validate(@Valid @RequestBody Body body) {}

    @GetMapping("/not-found")
    void notFound() {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Work item 9 not found");
    }

    @GetMapping("/missing")
    void missing() {
      throw new NotFoundException("Resource 9 not found");
    }

    @GetMapping("/already-there")
    void alreadyThere() {
      throw new ConflictException("Resource 1 is already in work item 1");
    }

    @GetMapping("/duplicate")
    void duplicate() {
      throw new DataIntegrityViolationException("duplicate key value violates unique constraint");
    }

    @GetMapping("/boom")
    void boom() {
      throw new IllegalStateException("internal secret");
    }
  }
}
