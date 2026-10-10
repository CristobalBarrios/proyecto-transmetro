package gt.muniguate.transmetro.operativo.catalogo.repository;

import gt.muniguate.transmetro.operativo.catalogo.domain.Municipalidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MunicipalidadRepository extends JpaRepository<Municipalidad, Integer> {
    List<Municipalidad> findAllByOrderByNombreAsc();
}
