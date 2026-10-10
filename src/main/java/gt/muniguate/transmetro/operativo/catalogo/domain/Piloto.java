package gt.muniguate.transmetro.operativo.catalogo.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "piloto")
public class Piloto {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false, length = 150)
    private String nombre;
    @Column(nullable = false, length = 250)
    private String residencia;
    @Column(nullable = false, length = 30)
    private String telefono;
    @Column(length = 150)
    private String correo;
    protected Piloto() { }
    public Piloto(String nombre, String residencia, String telefono, String correo) { actualizar(nombre, residencia, telefono, correo); }
    public void actualizar(String nombre, String residencia, String telefono, String correo) { this.nombre = nombre; this.residencia = residencia; this.telefono = telefono; this.correo = correo; }
    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
    public String getResidencia() { return residencia; }
    public String getTelefono() { return telefono; }
    public String getCorreo() { return correo; }
}
