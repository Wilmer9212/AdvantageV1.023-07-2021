/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fenoreste.rest.Dao;

import com.fenoreste.rest.ResponseDTO.AccountHoldersDTO;
import com.fenoreste.rest.ResponseDTO.validateMonetaryInstructionDTO;
import com.fenoreste.rest.Util.AbstractFacade;
import com.fenoreste.rest.Entidades.Auxiliares;
import com.fenoreste.rest.Entidades.transferencias_completadas_siscoop;
import com.fenoreste.rest.Entidades.validaciones_transferencias_siscoop;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

/**
 *
 * @author Elliot
 */
public abstract class FacadeTransfers<T> {

    private static EntityManagerFactory emf;

    public FacadeTransfers(Class<T> entityClass) {
        emf = AbstractFacade.conexion();//Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);    
    }

    public validateMonetaryInstructionDTO validateMonetaryInstruction(String customerId,
            String tipotransferencia,
            String cuentaorigen,
            String cuentadestino,
            Double montoTransferencia,
            String comentario,
            String propcuentadestino,
            String fechaejecucion,
            String tipoejecucion) {
        System.out.println("montoooooooooooooooooooooooooooooooooooooooo en facade:"+montoTransferencia);
        boolean bandera = false;
        Calendar c1 = Calendar.getInstance();
        String dia = Integer.toString(c1.get(Calendar.DATE));
        String mes = Integer.toString(c1.get(Calendar.MONTH) + 1);
        String annio = Integer.toString(c1.get(Calendar.YEAR));
        validateMonetaryInstructionDTO dto = null;
        EntityManager em = emf.createEntityManager();
        String[] fees = new String[0];
        String validationId = "";
        Query queryf=em.createNativeQuery("SELECT date(now())");
        String fe=String.valueOf(queryf.getSingleResult());
        Date hoy=new Date();
        try {
            System.out.println("aqui");
            if (findAccount(cuentaorigen, customerId) && findBalance(cuentaorigen, montoTransferencia)) {
                validationId = RandomAlfa().toUpperCase();
                System.out.println("aqui1");
                if (tipotransferencia.equalsIgnoreCase("transfer_own")) {
                    System.out.println("aqui2");
                    if (findAccount(cuentadestino, customerId)) {
                        EntityTransaction tr = em.getTransaction();
                        tr.begin();
                        validaciones_transferencias_siscoop vl = new validaciones_transferencias_siscoop();
                        vl.setCuentaorigen(cuentaorigen);
                        vl.setCuentadestino(cuentadestino);
                        vl.setTipotransferencia(tipotransferencia);
                        vl.setComentario1(comentario);
                        vl.setComentario2(propcuentadestino);
                        vl.setCustomerId(customerId);
                        vl.setFechaejecucion(hoy);
                        vl.setMonto(montoTransferencia);
                        vl.setTipoejecucion(tipoejecucion);
                        vl.setEstatus(false);
                        vl.setValidationId(validationId);
                        
                        em.persist(vl);
                        tr.commit();
                        bandera = true;
                    }
                }else if(tipotransferencia.toUpperCase().contains("INTRA")){
                   if (findAccount(cuentaorigen, customerId)) {
                        EntityTransaction tr = em.getTransaction();
                        tr.begin();
                        validaciones_transferencias_siscoop vl = new validaciones_transferencias_siscoop();
                        vl.setCuentaorigen(cuentaorigen);
                        vl.setCuentadestino(cuentadestino);
                        vl.setTipotransferencia(tipotransferencia);
                        vl.setComentario1(comentario);
                        vl.setComentario2(propcuentadestino);
                        vl.setCustomerId(customerId);
                        vl.setFechaejecucion(hoy);
                        vl.setMonto(montoTransferencia);
                        vl.setTipoejecucion(tipoejecucion);
                        vl.setEstatus(false);
                        vl.setValidationId(validationId);
                        
                        em.persist(vl);
                        tr.commit();
                        bandera = true;
                    } 
                }
            }

            if (bandera) {
                dto = new validateMonetaryInstructionDTO(validationId, fees, fe);
            }
        } catch (Exception e) {
            System.out.println("Error:" + e.getMessage());
            cerrar();
        }
        System.out.println("DTO:" + dto);
        return dto;
    }

    public String executeMonetaryInstruction(String validationId) {
        boolean bandera = false;
        Calendar c1 = Calendar.getInstance();
        String dia = Integer.toString(c1.get(Calendar.DATE));
        String mes = Integer.toString(c1.get(Calendar.MONTH) + 1);
        String annio = Integer.toString(c1.get(Calendar.YEAR));
        EntityManager em = emf.createEntityManager();
        String mensaje = "";
        try {
            String consulta = "SELECT * FROM v_transferenciassiscoop WHERE validationid='" + validationId + "' ORDER BY fechaejecucion DESC LIMIT 1";
            Query query = em.createNativeQuery(consulta, validaciones_transferencias_siscoop.class);
            validaciones_transferencias_siscoop vlt = (validaciones_transferencias_siscoop) query.getSingleResult();
            
            Query queryf=em.createNativeQuery("SELECT date(now())");
            String fe=String.valueOf(queryf.getSingleResult()).replace("-","/");
            System.out.println("fe:"+fe);
            Date hoy=stringToDate(fe);
            Query query1=em.createNativeQuery("SELECT saldo FROM auxiliares a where replace(to_char(a.idorigenp,'099999')||to_char(a.idproducto,'09999')||to_char(idauxiliar,'09999999'),' ','')='" + vlt.getCuentaorigen()+"'");
            Double saldo=Double.parseDouble(String.valueOf(query.getSingleResult()));
            
            System.out.println("hoy:");
            if (findBalance(vlt.getCuentaorigen(), vlt.getMonto())) {
                EntityTransaction tr = em.getTransaction();
                tr.begin();
                transferencias_completadas_siscoop vl = new transferencias_completadas_siscoop();
                vl.setCuentaorigen(vlt.getCuentaorigen());
                vl.setCuentadestino(vlt.getCuentadestino());
                vl.setTipotransferencia(vlt.getTipotransferencia());
                vl.setComentario1(vlt.getComentario1());
                vl.setComentario2(vlt.getComentario2());
                vl.setCustomerId(vlt.getCustomerId());
                vl.setFechaejecucion(hoy);
                vl.setMonto(vlt.getMonto());
                vl.setTipoejecucion(vlt.getTipoejecucion());
                vl.setEstatus(true);
                vl.setRunningBalance(saldo);
                em.persist(vl);
                tr.commit();
                bandera = true;
                
            }
            
            if (bandera && aplicarCargos(vlt.getCuentadestino(),vlt.getMonto(),1) && aplicarCargos(vlt.getCuentaorigen(),vlt.getMonto(),0)) {
              mensaje = "completed";
            }
        } catch (Exception e) {
            System.out.println("Error:" + e.getMessage());
            em.close();
        }
        return mensaje;
    }

    public List<AccountHoldersDTO> accountHolders(String accountId) {
        EntityManager em = emf.createEntityManager();
        List<AccountHoldersDTO> listaDTO = new ArrayList<AccountHoldersDTO>();
        try {
            String consulta = "SELECT p.nombre||' '||p.appaterno||' '||p.apmaterno as nombre FROM auxiliares a "
                    + " INNER JOIN personas p USING(idorigen,idgrupo,idsocio)"
                    + " WHERE replace(to_char(a.idorigenp,'099999')||to_char(a.idproducto,'09999')||to_char(a.idauxiliar,'09999999'),' ','')='" + accountId + "'";
            Query query = em.createNativeQuery(consulta);
            System.out.println("Consulta:" + consulta);
            String nombre = (String) query.getSingleResult();
            System.out.println("Nombre:" + nombre);
            AccountHoldersDTO dto = null;
            if (!nombre.equals("")) {
                dto = new AccountHoldersDTO(nombre, "SOW");
            }
            listaDTO.add(dto);
            System.out.println("ListaDTO:" + listaDTO);

        } catch (Exception e) {
            em.close();
            System.out.println("Error:" + e.getMessage());
        }
        return listaDTO;
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
    
    private boolean findAccount(String accountId, String customerId) {
        EntityManager em = emf.createEntityManager();
        boolean bandera = false;
        try {
            String consulta = "SELECT * FROM auxiliares a "
                    + "WHERE replace(to_char(a.idorigenp,'099999')||to_char(a.idproducto,'09999')||to_char(a.idauxiliar,'09999999'),' ','')='" + accountId + "'"
                    + " AND  replace(to_char(a.idorigen,'099999')||to_char(a.idgrupo,'09')||to_char(a.idsocio,'099999'),' ','')='" + customerId + "' AND estatus=2";
            Query query = em.createNativeQuery(consulta, Auxiliares.class);
            System.out.println("consulta:" + consulta);
            Auxiliares a = (Auxiliares) query.getSingleResult();
            if (a != null) {
                bandera = true;
            }
        } catch (Exception e) {
            System.out.println("Error en find opa:" + e.getMessage());
            return bandera;
        }
        return bandera;
    }

    private boolean aplicarCargos(String accountId, Double monto, int tipocargo) {
        EntityManager em = emf.createEntityManager();
        String ba = "SELECT * FROM auxiliares a WHERE replace(to_char(a.idorigenp,'099999')||to_char(a.idproducto,'09999')||to_char(a.idauxiliar,'09999999'),' ','')='" + accountId + "'";
        Query query = em.createNativeQuery(ba, Auxiliares.class);
        Auxiliares a = (Auxiliares) query.getSingleResult();
        Double l = Double.parseDouble(monto.toString());
        Double s = Double.parseDouble(a.getSaldo().toString());
        boolean bandera=false;
        try {
            if (tipocargo == 0) {
            BigDecimal saldor = new BigDecimal(s - l);
            EntityTransaction tr = em.getTransaction();
            tr.begin();
            a.setSaldo(saldor);
            em.persist(a);
            tr.commit();
            bandera=true;
        } else if (tipocargo == 1) {
            BigDecimal saldor = new BigDecimal(s + l);
            EntityTransaction tr = em.getTransaction();
            tr.begin();
            a.setSaldo(saldor);
            em.persist(a);
            tr.commit();
            bandera=true;
        }
        } catch (Exception e) {
            em.close();
            System.out.println("Error en cargos:"+e.getMessage());
        }finally{
            em.close();
        }
        return bandera;
    }

    private boolean findBalance(String accountId, Double monto) {
        EntityManager em = emf.createEntityManager();
        boolean bandera = false;
        System.out.println("Llegooooooooooooooo");
        try {
            String consulta = "SELECT * FROM auxiliares a "
                    + "WHERE replace(to_char(a.idorigenp,'099999')||to_char(a.idproducto,'09999')||to_char(a.idauxiliar,'09999999'),' ','')='" + accountId + "' AND estatus=2 AND saldo>=" + monto;
            Query query = em.createNativeQuery(consulta, Auxiliares.class);
            Auxiliares a = (Auxiliares) query.getSingleResult();
            if (a != null) {
                bandera = true;
            }
        } catch (Exception e) {
            em.close();
            System.out.println("Error en find balance:" + e.getMessage());
            return bandera;
        }
        em.close();
        return bandera;
    }

    public String dateToString(Date cadena) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        String cadenaStr = sdf.format(cadena);
        return cadenaStr;
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

    public void cerrar() {
        emf.close();
    }

}
