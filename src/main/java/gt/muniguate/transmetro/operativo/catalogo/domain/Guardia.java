package gt.muniguate.transmetro.operativo.catalogo.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "guardia")
public class Guardia {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false, length = 150)
    private String nombre;
    @Column(length = 30)
    private String telefono;
    protected Guardia() { }
    public Guardia(String nombre, String telefono) { actualizar(nombre, telefono); }
    public void actualizar(String nombre, String telefono) { this.nombre = nombre; this.telefono = telefono; }
    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
    public String getTelefono() { return telefono; }
}
