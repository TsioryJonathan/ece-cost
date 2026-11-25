package mg.tsioryjonathan.ece_cost.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import mg.tsioryjonathan.ece_cost.entity.Resource;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {
}
