package gt.muniguate.transmetro.operativo.catalogo.repository;

import gt.muniguate.transmetro.operativo.catalogo.domain.Guardia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GuardiaRepository extends JpaRepository<Guardia, Integer> {
    List<Guardia> findAllByOrderByNombreAsc();
}
