package gt.muniguate.transmetro.operativo.catalogo.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "bus")
public class Bus {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false, unique = true, length = 20)
    private String placa;
    @Column(name = "capacidad_maxima", nullable = false)
    private Integer capacidadMaxima;
    @Column(name = "linea_id")
    private Integer lineaId;
    @Column(name = "parqueo_id", nullable = false)
    private Integer parqueoId;
    @Column(nullable = false, length = 20)
    private String estado;
    protected Bus() { }
    public Bus(String placa, Integer capacidadMaxima, Integer lineaId, Integer parqueoId, String estado) {
        actualizar(placa, capacidadMaxima, lineaId, parqueoId, estado);
    }
    public void actualizar(String placa, Integer capacidadMaxima, Integer lineaId, Integer parqueoId, String estado) {
        this.placa = placa; this.capacidadMaxima = capacidadMaxima; this.lineaId = lineaId; this.parqueoId = parqueoId; this.estado = estado;
    }
    public Integer getId() { return id; }
    public String getPlaca() { return placa; }
    public Integer getCapacidadMaxima() { return capacidadMaxima; }
    public Integer getLineaId() { return lineaId; }
    public Integer getParqueoId() { return parqueoId; }
    public String getEstado() { return estado; }
}
