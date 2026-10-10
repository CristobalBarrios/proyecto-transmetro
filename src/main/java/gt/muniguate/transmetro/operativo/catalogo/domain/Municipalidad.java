package gt.muniguate.transmetro.operativo.catalogo.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "municipalidad")
public class Municipalidad {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false, unique = true, length = 120)
    private String nombre;
    protected Municipalidad() { }
    public Municipalidad(String nombre) { this.nombre = nombre; }
    public void actualizar(String nombre) { this.nombre = nombre; }
    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
}
