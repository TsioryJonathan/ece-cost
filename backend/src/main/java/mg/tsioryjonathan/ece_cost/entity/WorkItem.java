package mg.tsioryjonathan.ece_cost.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "work_item")
@Getter
@Setter
@NoArgsConstructor
public class WorkItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String name;

  @Column(nullable = false)
  private String unit;

  @OneToMany(mappedBy = "workItem", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<WorkItemResource> lines = new ArrayList<>();

  public void addLine(WorkItemResource line) {
    lines.add(line);
    line.setWorkItem(this);
  }
}
