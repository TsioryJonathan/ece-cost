package mg.tsioryjonathan.ece_cost.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import mg.tsioryjonathan.ece_cost.entity.WorkItem;

@Repository
public interface WorkItemRepository extends JpaRepository<WorkItem, Long> {

  @Query("""
      select distinct w from WorkItem w
      left join fetch w.lines l
      left join fetch l.resource
      """)
  List<WorkItem> findAllWithLines();

  @Query("""
      select w from WorkItem w
      left join fetch w.lines l
      left join fetch l.resource
      where w.id = :id
      """)
  Optional<WorkItem> findByIdWithLines(Long id);
}
