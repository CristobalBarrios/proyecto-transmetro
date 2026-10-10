package gt.muniguate.transmetro.operativo.catalogo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "linea")
public class Linea {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "municipalidad_id", nullable = false)
    private Integer municipalidadId;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, length = 15)
    private String estado;

    protected Linea() {
    }

    public Linea(Integer municipalidadId, String codigo, String nombre) {
        this.municipalidadId = municipalidadId;
        this.codigo = codigo;
        this.nombre = nombre;
        this.estado = "borrador";
    }

    public void actualizar(Integer municipalidadId, String codigo, String nombre) {
        this.municipalidadId = municipalidadId;
        this.codigo = codigo;
        this.nombre = nombre;
    }

    public Integer getId() { return id; }
    public Integer getMunicipalidadId() { return municipalidadId; }
    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
}
