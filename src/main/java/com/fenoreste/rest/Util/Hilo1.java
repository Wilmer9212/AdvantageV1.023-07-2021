/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fenoreste.rest.Util;

import java.io.File;

/**
 *
 * @author wilmer
 */
public class Hilo1 implements Runnable {
    public String nombreHilo;
    public File file;
    public Hilo1(String nombre,File fileR){
        nombreHilo=nombre;
        file=fileR;
    }
    //Punto de entrada del hilo
    //Los hilos comienzan a ejecutarse aquí
    public void run(){
        System.out.println("Comenzando "+nombreHilo);
        try {
            for (int contar=0; contar<15; contar++){
                Thread.sleep(1000);
                if(contar==14){
                    file.delete();
                }
                System.out.println("En "+nombreHilo+", el recuento "+contar);
            }
        }catch (InterruptedException exc){
            System.out.println(nombreHilo + "Interrumpido.");
        }
        System.out.println("Terminando "+nombreHilo);
    }
}
