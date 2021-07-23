/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fenoreste.rest.Dao;

import com.fenoreste.rest.Entidades.e_Alerts;
import com.fenoreste.rest.Entidades.v_Alertas;
import com.fenoreste.rest.Util.AbstractFacade;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

/**
 *
 * @author Elliot
 */
public abstract class FacadeAlerts<T> {

    private static EntityManagerFactory emf;

    public FacadeAlerts(Class<T> entityClass) {
        emf = AbstractFacade.conexion();
    }

    public String validateAlert(String customerId, String alertCode, boolean enabled) {
        EntityManager em = emf.createEntityManager();
        boolean bandera=false;
        try {
        Calendar c1 = Calendar.getInstance();
        String dia = Integer.toString(c1.get(Calendar.DATE));
        String mes = Integer.toString(c1.get(Calendar.MONTH) + 1);
        String annio = Integer.toString(c1.get(Calendar.YEAR));
        String validationId = "";
        Query queryf = em.createNativeQuery("SELECT date(now())");
        String fe = String.valueOf(queryf.getSingleResult());
        Date hoy = stringToDate(fe.replace("-","/"));
        System.out.println("aqui");       
                validationId = RandomAlfa().toUpperCase();
                       if (findPersona(customerId)) {
                        EntityTransaction tr = em.getTransaction();
                        tr.begin();
                        v_Alertas vl = new v_Alertas();
                        vl.setCode(alertCode);
                        vl.setCustomerid(customerId);
                        vl.setEnabled(true);
                        vl.setFechaejecucion(hoy);  
                        vl.setValidattionid(validationId);
                        em.persist(vl);
                        tr.commit();
                        bandera = true;
                    }
         if(bandera){
             return validationId;
         }
        } catch (Exception e) {
            System.out.println("Error al generar codigo de validacion:" + e.getMessage());
        }
       return null;
    }
    
    
    public String executeAlert(String validationId) {
        EntityManager em = emf.createEntityManager();
        boolean bandera=false;
        try {
        Calendar c1 = Calendar.getInstance();
        String consulta="SELECT * FROM v_alertas WHERE validationid='"+validationId+"'";
        Query query1=em.createNativeQuery(consulta,v_Alertas.class);
        v_Alertas va=(v_Alertas) query1.getSingleResult();
        Query queryf = em.createNativeQuery("SELECT date(now())");
        String fe = String.valueOf(queryf.getSingleResult());
        Date hoy = stringToDate(fe.replace("-","/"));
                        EntityTransaction tr = em.getTransaction();
                        tr.begin();
                        e_Alerts vl = new e_Alerts();
                        vl.setCode(va.getCode());
                        vl.setCustomerid(va.getCustomerid());
                        vl.setEnabled(true);
                        vl.setFechaejecucion(hoy);  
                        em.persist(vl);
                        tr.commit();
                        bandera = true;
                    System.out.println("Bandera:"+bandera);
         if(bandera){
             return "completed";
         }else{
             return "rejected";
         }
        } catch (Exception e) {
            System.out.println("Error al ejecutar:" + e.getMessage());
        }
       return "Fail";
    }

    
    public Date stringToDate(String cadena) {
        SimpleDateFormat formato = new SimpleDateFormat("yyyy/MM/dd");
        Date fechaDate = null;

        try {
            fechaDate = formato.parse(cadena);
        } catch (Exception ex) {
            System.out.println("Error fecha:" + ex.getMessage());
        }
        System.out.println("fechaDate:" + fechaDate);
        return fechaDate;
    }

     public String RandomAlfa() {
        String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
        String CHAR_UPPER = CHAR_LOWER.toUpperCase();
        String NUMBER = "0123456789";

        String DATA_FOR_RANDOM_STRING = CHAR_LOWER + CHAR_UPPER + NUMBER;
        SecureRandom random = new SecureRandom();

        String cadena = "";
        for (int i = 0; i < 15; i++) {
            int rndCharAt = random.nextInt(DATA_FOR_RANDOM_STRING.length());
            char rndChar = DATA_FOR_RANDOM_STRING.charAt(rndCharAt);
            cadena = cadena + rndChar;
        }
        System.out.println("Cadena:" + cadena);
        return cadena;
    }
     
     public boolean findPersona(String customerId){
         EntityManager em=emf.createEntityManager();
         boolean bandera=false;
         try {
             Query query=em.createNativeQuery("SELECT count(*) FROM personas WHERE "
                     + "replace(to_char(idorigen,'099999')||to_char(idgrupo,'09')||to_char(idsocio,'099999'),' ','')='"+customerId+"'");
             int count=Integer.parseInt(String.valueOf(query.getSingleResult()));
             System.out.println("count:"+count);
             if(count>0){
             bandera=true;    
             }
         } catch (Exception e) {
             System.out.println("Error al buscar persona:"+e.getMessage());
         } 
         return bandera;
     }
     
     public void cerrar(){
         emf.close();
     }
     
}
