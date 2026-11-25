package mg.tsioryjonathan.ece_cost.api;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import mg.tsioryjonathan.ece_cost.entity.Resource;
import mg.tsioryjonathan.ece_cost.entity.WorkItem;
import mg.tsioryjonathan.ece_cost.repository.ResourceRepository;
import mg.tsioryjonathan.ece_cost.repository.WorkItemRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class WorkItemApiTest {

  @Autowired
  MockMvc mvc;
  @Autowired
  ResourceRepository resources;
  @Autowired
  WorkItemRepository workItems;

  @Test
  void listsTheSeedWorkItemsWithTheirComputedPrices() throws Exception {
    ResultActions result = mvc.perform(get("/api/work-items")).andExpect(status().isOk());

    result.andExpect(jsonPath("$.length()").value(4));
    result.andExpect(jsonPath("$[?(@.name=='Béton de fondation')].salePrice").value(contains(404325)));
    result.andExpect(jsonPath("$[?(@.name=='Enduit de façade')].salePrice").value(contains(16673)));
    result.andExpect(jsonPath("$[?(@.name=='Terrassement en fouille')].salePrice").value(contains(19913)));
    result.andExpect(jsonPath("$[?(@.name=='Dallage béton 10 cm')].salePrice").value(contains(42458)));
    result.andExpect(jsonPath("$[?(@.name=='Béton de fondation')].directCost").value(contains(299500)));
  }

  @Test
  void workItemsAreSortedByIdAndLinesAndResourcesByNameInFrenchOrder() throws Exception {
    mvc.perform(get("/api/work-items"))
        .andExpect(jsonPath("$[0].name").value("Béton de fondation"))
        .andExpect(jsonPath("$[1].name").value("Enduit de façade"))
        .andExpect(jsonPath("$[2].name").value("Terrassement en fouille"))
        .andExpect(jsonPath("$[3].name").value("Dallage béton 10 cm"))
        .andExpect(jsonPath("$[0].lines[0].resourceName").value("Bétonnière"))
        .andExpect(jsonPath("$[0].lines[1].resourceName").value("Ciment"));
    mvc.perform(get("/api/resources"))
        .andExpect(jsonPath("$[0].name").value("Bétonnière"))
        .andExpect(jsonPath("$[1].name").value("Ciment"));
  }

  @Test
  void changingTheCementPriceMovesEveryWorkItemThatUsesIt() throws Exception {
    Resource cement = resourceNamed("Ciment");

    mvc.perform(patch("/api/resources/{id}", cement.getId()).contentType(MediaType.APPLICATION_JSON)
        .content("{\"unitPrice\": 30000}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.unitPrice").value(30000));

    mvc.perform(get("/api/work-items"))
        .andExpect(jsonPath("$[?(@.name=='Béton de fondation')].salePrice").value(contains(423225)))
        .andExpect(jsonPath("$[?(@.name=='Enduit de façade')].salePrice").value(contains(17348)))
        .andExpect(jsonPath("$[?(@.name=='Dallage béton 10 cm')].salePrice").value(contains(44348)))
        .andExpect(jsonPath("$[?(@.name=='Terrassement en fouille')].salePrice").value(contains(19913)));
  }

  @Test
  void acceptsAZeroUnitPrice() throws Exception {
    Resource cement = resourceNamed("Ciment");
    mvc.perform(patch("/api/resources/{id}", cement.getId()).contentType(MediaType.APPLICATION_JSON).content("{\"unitPrice\": 0}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.unitPrice").value(0));
  }

  @Test
  void rejectsANegativeOrTooPreciseUnitPrice() throws Exception {
    Resource cement = resourceNamed("Ciment");
    for (String body : new String[] { "{\"unitPrice\": -5}", "{\"unitPrice\": 12.345}", "{}" }) {
      mvc.perform(patch("/api/resources/{id}", cement.getId()).contentType(MediaType.APPLICATION_JSON).content(body))
          .andExpect(status().isBadRequest())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
          .andExpect(jsonPath("$.status").value(400))
          .andExpect(jsonPath("$.detail").value(startsWith("unitPrice: ")));
    }
  }

  @Test
  void updatingTheUnitPriceOfAnUnknownResourceIs404() throws Exception {
    mvc.perform(patch("/api/resources/{id}", 999999).contentType(MediaType.APPLICATION_JSON).content("{\"unitPrice\": 100}"))
        .andExpect(status().isNotFound())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404));
  }

  @Test
  void addingALineReturnsTheWorkItemRecalculated() throws Exception {
    WorkItem foundation = workItemNamed("Béton de fondation");
    Resource laborer = resourceNamed("Manœuvre");

    mvc.perform(post("/api/work-items/{id}/resources", foundation.getId()).contentType(MediaType.APPLICATION_JSON)
        .content("{\"resourceId\": " + laborer.getId() + ", \"quantity\": 0.4}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.lines.length()").value(6))
        .andExpect(jsonPath("$.directCost").value(300500))
        .andExpect(jsonPath("$.salePrice").value(405675));

    mvc.perform(get("/api/work-items"))
        .andExpect(jsonPath("$[?(@.name=='Béton de fondation')].salePrice").value(contains(405675)));
  }

  @Test
  void addingTheSameResourceTwiceIs409() throws Exception {
    WorkItem foundation = workItemNamed("Béton de fondation");
    Resource cement = resourceNamed("Ciment");

    mvc.perform(post("/api/work-items/{id}/resources", foundation.getId()).contentType(MediaType.APPLICATION_JSON)
        .content("{\"resourceId\": " + cement.getId() + ", \"quantity\": 1}"))
        .andExpect(status().isConflict())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(409));
  }

  @Test
  void acceptsAQuantityWithFourDecimals() throws Exception {
    WorkItem foundation = workItemNamed("Béton de fondation");
    Resource laborer = resourceNamed("Manœuvre");
    mvc.perform(post("/api/work-items/{id}/resources", foundation.getId()).contentType(MediaType.APPLICATION_JSON)
        .content("{\"resourceId\": " + laborer.getId() + ", \"quantity\": 0.1234}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.lines[?(@.resourceName=='Manœuvre')].quantity").value(contains(0.1234)));
  }

  @Test
  void rejectsAMissingZeroNegativeOrTooPreciseQuantity() throws Exception {
    WorkItem foundation = workItemNamed("Béton de fondation");
    Resource laborer = resourceNamed("Manœuvre");
    for (String quantity : new String[] { "0", "-1", "0.12345", "null" }) {
      mvc.perform(post("/api/work-items/{id}/resources", foundation.getId()).contentType(MediaType.APPLICATION_JSON)
          .content("{\"resourceId\": " + laborer.getId() + ", \"quantity\": " + quantity + "}"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.detail").value(startsWith("quantity: ")));
    }
  }

  @Test
  void unknownWorkItemOrResourceIs404() throws Exception {
    WorkItem foundation = workItemNamed("Béton de fondation");
    mvc.perform(post("/api/work-items/{id}/resources", 999999).contentType(MediaType.APPLICATION_JSON)
        .content("{\"resourceId\": 1, \"quantity\": 1}"))
        .andExpect(status().isNotFound());
    mvc.perform(post("/api/work-items/{id}/resources", foundation.getId()).contentType(MediaType.APPLICATION_JSON)
        .content("{\"resourceId\": 999999, \"quantity\": 1}"))
        .andExpect(status().isNotFound());
  }

  private Resource resourceNamed(String name) {
    return resources.findAll().stream().filter(r -> r.getName().equals(name)).findFirst().orElseThrow();
  }

  private WorkItem workItemNamed(String name) {
    return workItems.findAll().stream().filter(w -> w.getName().equals(name)).findFirst().orElseThrow();
  }
}
