package gt.muniguate.transmetro.operativo.catalogo.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "parqueo")
public class Parqueo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "estacion_id")
    private Integer estacionId;
    @Column(nullable = false, unique = true, length = 120)
    private String nombre;
    protected Parqueo() { }
    public Parqueo(Integer estacionId, String nombre) { this.estacionId = estacionId; this.nombre = nombre; }
    public void actualizar(Integer estacionId, String nombre) { this.estacionId = estacionId; this.nombre = nombre; }
    public Integer getId() { return id; }
    public Integer getEstacionId() { return estacionId; }
    public String getNombre() { return nombre; }
}
