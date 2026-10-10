package gt.muniguate.transmetro.operativo.catalogo.repository;

import gt.muniguate.transmetro.operativo.catalogo.domain.Parqueo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParqueoRepository extends JpaRepository<Parqueo, Integer> {
    List<Parqueo> findAllByOrderByNombreAsc();
}
