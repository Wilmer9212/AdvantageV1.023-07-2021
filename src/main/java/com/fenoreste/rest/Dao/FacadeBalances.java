/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fenoreste.rest.Dao;

import com.fenoreste.rest.Util.AbstractFacade;
import com.fenoreste.rest.Entidades.Auxiliares;
import com.fenoreste.rest.Entidades.Productos;
import com.fenoreste.rest.ResponseDTO.BalancesDTO;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

/**
 *
 * @author Elliot
 */
public abstract class FacadeBalances<T> {

    private static EntityManagerFactory emf;
    List<Object[]> lista = null;

    public FacadeBalances(Class<T> entityClass) {
        emf = AbstractFacade.conexion();
    }

    public ArrayList<BalancesDTO> balances(String[]accountsId) {
        EntityManager em = emf.createEntityManager();
        System.out.println("length:"+accountsId.length);
        String op = "", aa = "";
        Double Ledger = 0.0;
        Double Avalaible = 0.0;
        ArrayList<BalancesDTO>dtos=new ArrayList<BalancesDTO>();
        try {
            int i=0;
            for(i=0;i<accountsId.length;i++){
            String accountId=accountsId[i];
                System.out.println("AccountID:"+accountId);
            String c = "SELECT * FROM auxiliares a"
                    + " INNER JOIN productos pr USING(idproducto)"
                    + " WHERE replace(to_char(a.idorigenp,'099999')||"
                    + "to_char(a.idproducto,'09999')||to_char(a.idauxiliar,'09999999'),' ','')='" + accountId + "'";
            System.out.println("Consulta:" + c);
            Query query = em.createNativeQuery(c, Auxiliares.class);
            Auxiliares a = (Auxiliares) query.getSingleResult();
            Productos pr = em.find(Productos.class, a.getAuxiliaresPK().getIdproducto());

            op = String.format("%06d", a.getAuxiliaresPK().getIdorigenp()) + String.format("%05d", a.getAuxiliaresPK().getIdproducto());
            aa = String.format("%08d", a.getAuxiliaresPK().getIdauxiliar());
            Ledger = Double.parseDouble(a.getSaldo().toString());
            Double garantia = 0.0;
            if (pr.getTipoproducto() == 1) {
                System.out.println("es un dpf");
                //Se suma fechaactivacion mas plazos para determinar si el producto ya se puede cobrar o aun no
                String cc = "SELECT a.fechaactivacion + " + Integer.parseInt(String.valueOf(a.getPlazo())) + " FROM auxiliares a WHERE a.idorigenp="
                        + a.getAuxiliaresPK().getIdorigenp()
                        + " AND a.idproducto=" + a.getAuxiliaresPK().getIdproducto()
                        + " AND a.idauxiliar=" + a.getAuxiliaresPK().getIdauxiliar();
                System.out.println("ConsultaDPF:" + cc);
                Query query_vencimiento_dpf = em.createNativeQuery(cc);
                Query fecha_actual_servidor = em.createNativeQuery("SELECT date(now())");
                Date fecha_servidor = ParseFecha(String.valueOf(fecha_actual_servidor.getSingleResult()).replace("-", "/"));
                Date fecha_vencimiento_dpf = ParseFecha(String.valueOf(query_vencimiento_dpf.getSingleResult()).replace("-", "/"));
                
                BigDecimal bd = new BigDecimal(Ledger);                
                bd = bd.setScale(2, RoundingMode.HALF_UP);
                if (fecha_servidor == fecha_vencimiento_dpf || fecha_servidor.after(fecha_vencimiento_dpf)) {
                    if (Double.parseDouble(a.getGarantia().toString()) > 0) {
                        garantia = garantia + Double.parseDouble(a.getGarantia().toString());
                        Avalaible = Ledger - garantia;
                    } else {
                        Avalaible = Ledger;
                    }
                }

            } else if (pr.getTipoproducto() == 0) {
                System.out.println("Es un ahorro");
                if (Double.parseDouble(a.getGarantia().toString()) > 0) {
                    garantia = garantia + Double.parseDouble(a.getGarantia().toString());
                    Avalaible = Ledger - garantia;
                } else {
                    Avalaible = Ledger;
                }
            }
            BalancesDTO dto=new BalancesDTO(accountId, Ledger, Avalaible);
            dtos.add(dto);
        }
            //}Terminaba el segundo for          
            ///}Terminaba el for
        } catch (Exception e) {
            e.getStackTrace();
            em.close();
            System.out.println("Error:" + e.getMessage());
        }
        System.out.println("Ledger:" + Ledger);
        System.out.println("Avalaible:" + Avalaible);
        Double saldos[] = new Double[2];

       /* saldos[0] = Ledger;
        saldos[1] = Avalaible;*/
        em.close();
        return dtos;//saldos;
    }

    public String dateToString(Date cadena) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        String cadenaStr = sdf.format(cadena);
        return cadenaStr;
    }

    public static Date ParseFecha(String fecha) {
        SimpleDateFormat formato = new SimpleDateFormat("yyyy/MM/dd");
        Date fechaDate = null;
        try {
            fechaDate = formato.parse(fecha);
        } catch (ParseException ex) {
            System.out.println(ex);
        }
        return fechaDate;
    }

    public Date stringToDate(String cadena) {
        cadena = "2021/06/18";
        SimpleDateFormat formato = new SimpleDateFormat("yyyy/MM/dd");

        Date fechaDate = null;
        System.out.println("aqui");
        try {
            System.out.println("cdena:" + cadena);
            fechaDate = formato.parse(cadena);
            System.out.println("DateConver:" + fechaDate);
        } catch (Exception ex) {
            System.out.println("Error fecha:" + ex.getMessage());
        }
        System.out.println("fechaDate:" + fechaDate);
        return fechaDate;
    }

    public void cerrar() {
        emf.close();
    }
}
