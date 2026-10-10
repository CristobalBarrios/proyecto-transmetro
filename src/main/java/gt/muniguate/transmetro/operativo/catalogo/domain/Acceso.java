package gt.muniguate.transmetro.operativo.catalogo.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "acceso")
public class Acceso {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "estacion_id", nullable = false)
    private Integer estacionId;
    @Column(nullable = false, length = 30)
    private String codigo;
    @Column(length = 200)
    private String descripcion;
    protected Acceso() { }
    public Acceso(Integer estacionId, String codigo, String descripcion) { actualizar(estacionId, codigo, descripcion); }
    public void actualizar(Integer estacionId, String codigo, String descripcion) { this.estacionId = estacionId; this.codigo = codigo; this.descripcion = descripcion; }
    public Integer getId() { return id; }
    public Integer getEstacionId() { return estacionId; }
    public String getCodigo() { return codigo; }
    public String getDescripcion() { return descripcion; }
}
