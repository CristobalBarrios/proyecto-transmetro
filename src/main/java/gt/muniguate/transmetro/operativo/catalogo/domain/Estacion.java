package gt.muniguate.transmetro.operativo.catalogo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "estacion")
public class Estacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "municipalidad_id", nullable = false)
    private Integer municipalidadId;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, length = 250)
    private String direccion;

    @Column(nullable = false)
    private short zona;

    @Column(name = "codigo_ruta", nullable = false, length = 30)
    private String codigoRuta;

    protected Estacion() {
    }

    public Estacion(Integer municipalidadId, String nombre, String direccion, short zona, String codigoRuta) {
        this.municipalidadId = municipalidadId;
        this.nombre = nombre;
        this.direccion = direccion;
        this.zona = zona;
        this.codigoRuta = codigoRuta;
    }

    public void actualizar(String nombre, String direccion, short zona, String codigoRuta) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.zona = zona;
        this.codigoRuta = codigoRuta;
    }

    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDireccion() { return direccion; }
    public short getZona() { return zona; }
    public String getCodigoRuta() { return codigoRuta; }
}
