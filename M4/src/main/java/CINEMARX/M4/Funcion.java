/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package CINEMARX.M4;

/**
 *
 * @author gaels
 */
public class Funcion {
    private int id;
    private int peliculaId;
    private String fecha;
    private String hora;
    private String idioma;
    private String formato;
    private int salaId;
    
    public Funcion(int id, int peliculaId, String fecha, String hora, 
                   String idioma, String formato, int salaId) {
        this.id = id;
        this.peliculaId = peliculaId;
        this.fecha = fecha;
        this.hora = hora;
        this.idioma = idioma;
        this.formato = formato;
        this.salaId = salaId;
    }
    
    // Getters
    public int getId() { return id; }
    public int getPeliculaId() { return peliculaId; }
    public String getFecha() { return fecha; }
    public String getHora() { return hora; }
    public String getIdioma() { return idioma; }
    public String getFormato() { return formato; }
    public int getSalaId() { return salaId; }
}