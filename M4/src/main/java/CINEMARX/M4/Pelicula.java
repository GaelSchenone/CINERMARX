/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package CINEMARX.M4;

/**
 *
 * @author gaels
 */
public class Pelicula {
    private final int id;
    private final String titulo;
    private final String imagen;
    private final String clasificacion;
    private final String formato;
    private final String duracion;
    private final String fechaEstreno;
    private final String descripcion;
    
    public Pelicula(int id, String titulo, String imagen, String clasificacion, 
                    String formato, String duracion, String fechaEstreno, String descripcion) {
        this.id = id;
        this.titulo = titulo;
        this.imagen = imagen;
        this.clasificacion = clasificacion;
        this.formato = formato;
        this.duracion = duracion;
        this.fechaEstreno = fechaEstreno;
        this.descripcion = descripcion;
    }
    
    // Getters
    public int getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getImagen() { return imagen; }
    public String getClasificacion() { return clasificacion; }
    public String getFormato() { return formato; }
    public String getDuracion() { return duracion; }
    public String getFechaEstreno() { return fechaEstreno; }
    public String getDescripcion() { return descripcion; }
}