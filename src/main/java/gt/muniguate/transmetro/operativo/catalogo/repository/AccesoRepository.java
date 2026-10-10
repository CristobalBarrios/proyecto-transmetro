package gt.muniguate.transmetro.operativo.catalogo.repository;

import gt.muniguate.transmetro.operativo.catalogo.domain.Acceso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccesoRepository extends JpaRepository<Acceso, Integer> {
    List<Acceso> findAllByOrderByCodigoAsc();
}
