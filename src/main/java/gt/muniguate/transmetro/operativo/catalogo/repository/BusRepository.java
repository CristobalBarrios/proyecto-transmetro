package gt.muniguate.transmetro.operativo.catalogo.repository;

import gt.muniguate.transmetro.operativo.catalogo.domain.Bus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BusRepository extends JpaRepository<Bus, Integer> {
    List<Bus> findAllByOrderByPlacaAsc();
}
