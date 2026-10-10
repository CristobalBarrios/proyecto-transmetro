package gt.muniguate.transmetro.operativo.catalogo.repository;

import gt.muniguate.transmetro.operativo.catalogo.domain.Linea;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LineaRepository extends JpaRepository<Linea, Integer> {
    java.util.List<Linea> findAllByOrderByCodigoAsc();
}
