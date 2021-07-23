/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fenoreste.rest.services;

import com.fenoreste.rest.Util.AbstractFacade;
import com.fenoreste.rest.Util.Hilo1;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import javax.json.Json;
import javax.ws.rs.POST;
import org.json.JSONObject;
import org.xhtmlrenderer.pdf.ITextRenderer;
/**
 *
 * @author Elliot
 */
@Path("api/file")
 public class FileResources {
   
   @POST
   @Produces({MediaType.APPLICATION_JSON})
   @Consumes({MediaType.APPLICATION_JSON})
   public Response fileDownload(String cadena){
       System.out.println("cadena:"+cadena);
       JSONObject RequestData=new JSONObject(cadena);
       String fileId="";
       try{
           fileId=RequestData.getString("fileId");           
       }catch(Exception e){
           return Response.status(Response.Status.BAD_GATEWAY).entity(e.getMessage()).build();
       }
    if(!fileId.equals("")){
        File file=new File(ruta()+fileId+".pdf");
        if(file.exists()){
        Response.ResponseBuilder response = Response.ok(file);
        response.header("Content-Disposition", "attachment; filename=estado_cuenta_ahorro.pdf");
       /* Hilo1 mh=new Hilo1("#1",file);
        Thread nuevoh=new Thread(mh);
        nuevoh.start();*/
        return response.build();
        }else{
            javax.json.JsonObject jsonError=null;
            System.out.println("Error Message:No existe el archivo"+fileId);
            jsonError=Json.createObjectBuilder().add("Error","No existe el archivo que intenta descargar").build();
            return Response.status(Response.Status.BAD_GATEWAY).entity(jsonError).build();
        }
        
    }
    return null;
   }
   
   
   
   
   @GET
   @Path("/create")
   @Produces({MediaType.APPLICATION_JSON})
   @Consumes({MediaType.APPLICATION_JSON})
   public Response files(String cadena) throws FileNotFoundException{
   crearHTML();
   crearPDF(ruta(),"estado.html");
   return null;
    
}

public void crearHTML() throws FileNotFoundException{
    String txt=ruta()+"estado_cuenta_1010.txt";
    System.out.println("tt:"+txt);
    File file = new File(txt);
    if(file.exists()){
        FileOutputStream fs=new FileOutputStream(ruta()+"estado.html");
        OutputStreamWriter out=new OutputStreamWriter(fs);
      try {
                FileReader fr = new FileReader(file);
                BufferedReader br = new BufferedReader(fr);
                String linea;
                while ((linea = br.readLine()) != null) {
                    if(linea.contains("src")){
                        System.out.println("linea:"+linea.trim());
                    }
                    if(linea.contains(" & ")){
                       linea=linea.replace("&","y");
                    }
                    out.write(linea);                    
                }
                out.close();
            } catch (Exception e) {
                System.out.println("Excepcion leyendo txt" +  ": " + e.getMessage());
            }
        
    }else{
        System.out.println("Error fichero que intenta cargar no existe");
    }
}

public boolean crearPDF(String ruta, String nombreHTML) {
     String linea="";   
    try {
            String ficheroHTML =ruta+nombreHTML;
            File file=new File(ficheroHTML);
            FileReader fr = new FileReader(file);
           
                BufferedReader br = new BufferedReader(fr);
                
                while ((linea = br.readLine()) != null) {
                   if(linea.contains("<br />")){
                       linea="</br>";
                   }                    
                }
                
                
            System.out.println("fichero"+ficheroHTML);
            String url = new File(ficheroHTML).toURI().toURL().toString();
            String ficheroPDF = ruta+nombreHTML.replace(".html",".pdf");
            OutputStream os = new FileOutputStream(ficheroPDF);
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocument(url);
            renderer.layout();
            renderer.createPDF(os);
            os.close();
            return true;
        } catch (Exception e) {
            System.out.println("Error al crear PDF:"+e.getMessage());
        return false;
        }       
        
    }
   
   
   
   @GET
   @Produces({MediaType.APPLICATION_JSON})
   @Consumes({MediaType.APPLICATION_JSON})
   public Response file(){
       System.out.println("EntroAGateway");
       EntityManagerFactory emf=AbstractFacade.conexion();
       EntityManager em=emf.createEntityManager();
       runFileCreate();
       try {
       System.out.println("Hola paso");
       String con="select sai_estado_cuenta_ahorros(010101,00110,00010667,'01/04/2021','30/04/2021')";    
       Query query=em.createNativeQuery(con);
       String cadena=String.valueOf(query.getSingleResult());
           System.out.println("Cadena:"+cadena);
       } catch (Exception e) {
           em.close();
           emf.close();
           System.out.println("Error:"+e.getMessage());
       }
       emf.close();
   return null;
    
}
 
   
 String opa="1001-20-2109";
 String FicheroTxt="estado_cuenta_"+opa+".txt";
 String FicheroHTML="estado_cuenta_"+opa+".html";
 
 public static String ruta(){
   String home=System.getProperty("user.home");
   String separador=System.getProperty("file.separator");
   return home+separador+"Banca"+separador;
  }
 
 public FileOutputStream runFileCreate(){
  System.out.println("Run file");
  File f1=new File(ruta()+FicheroTxt);
  FileOutputStream fs =null;
  try{
  if(f1.exists()){
     System.out.println("Existia y se elimino el txt");
     f1.delete();
     System.out.println("Se crea y llena el Txt");
     crearLllenarTxt(opa);
     File f2=new File(ruta()+FicheroHTML);
     if(f2.exists()){
         System.out.println("Existia y se elimino el html");
         f2.delete();
         System.out.println("Se crea el HTML");
         fs=new FileOutputStream(ruta()+FicheroHTML);
         System.out.println("Se llena el html");
         llenarHTML(obtenerTxt(),fs);
     }else{
         System.out.println("no Existe txt");
     }
  }else{//Si no existe txt
      System.out.println(" al else");
     crearLllenarTxt(opa);
     File f2=new File(ruta()+FicheroHTML);
     if(f2.exists()){
         System.out.println("Existia y se elimino el html");
         f2.delete();
         System.out.println("Se crea el HTML");
         fs=new FileOutputStream(ruta()+FicheroHTML);
         System.out.println("Se llena el html");
         llenarHTML(obtenerTxt(),fs);
     }else{
         fs=new FileOutputStream(ruta()+FicheroHTML);
         System.out.println("Se llena el html");
         llenarHTML(obtenerTxt(),fs);
     }
  }
  }catch(Exception e){
      System.out.println("Error:"+e.getMessage());
  }
  return fs;
 }
  public  void crearLllenarTxt(String opa){
    EntityManagerFactory emf=AbstractFacade.conexion();
    try { 
            EntityManager em=emf.createEntityManager();
            String ruta = ruta()+FicheroTxt;
            String contenido;
            String consulta="select sai_estado_cuenta_ahorros(010101,00110,00010667,'01/01/2020','30/04/2020')";
            Query query=em.createNativeQuery(consulta);
            contenido=String.valueOf(query.getSingleResult());
            File file = new File(ruta);
            // Si el archivo no existe es creado
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(contenido);
            bw.close();
            em.clear();
            em.close();
        } catch (Exception e) {
            emf.close();
            e.printStackTrace();
            System.out.println("Error:"+e.getMessage());
        }
    emf.close();
    }
  
     public File obtenerTxt() {
        String sf = ruta()+ FicheroTxt;
        File f = new File(sf);
        if (f.exists()) {
            return f;
        } else {
            System.out.println("El fichero no existe: " + sf);
            return null;
        }
    }
 
     public static void llenarHTML(File file,FileOutputStream fs){
     OutputStreamWriter out=new OutputStreamWriter(fs);
      try {
                FileReader fr = new FileReader(file);
                BufferedReader br = new BufferedReader(fr);
                String linea;
                while ((linea = br.readLine()) != null) {
                    System.out.println("linea:"+linea);
                    if(linea.contains("src")){
                        System.out.println("linea:"+linea);
                    }
                    out.write(linea);                    
                }
                out.close();
            } catch (Exception e) {
                System.out.println("Excepcion leyendo txt" +  ": " + e.getMessage());
            }
        }
}