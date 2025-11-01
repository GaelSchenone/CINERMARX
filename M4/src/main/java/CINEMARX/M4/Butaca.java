/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package CINEMARX.M4;

/**
 *
 * @author gaels
 */
public class Butaca {
    private int fila;
    private int columna;
    private boolean ocupada;
    private boolean seleccionada;
    
    public Butaca(int fila, int columna, boolean ocupada) {
        this.fila = fila;
        this.columna = columna;
        this.ocupada = ocupada;
        this.seleccionada = false;
    }
    
    public int getFila() { return fila; }
    public int getColumna() { return columna; }
    public boolean isOcupada() { return ocupada; }
    public boolean isSeleccionada() { return seleccionada; }
    public void setSeleccionada(boolean seleccionada) { this.seleccionada = seleccionada; }
}