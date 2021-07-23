/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fenoreste.rest.Dao;

import com.fenoreste.rest.ResponseDTO.AccountHoldersDTO;
import com.fenoreste.rest.ResponseDTO.DetailsAccountDTO;
import com.fenoreste.rest.ResponseDTO.HoldsDTO;
import com.fenoreste.rest.ResponseDTO.StatementsDTO;
import com.fenoreste.rest.Util.AbstractFacade;
import com.fenoreste.rest.Entidades.Auxiliares;
import com.fenoreste.rest.Entidades.AuxiliaresD;
import com.fenoreste.rest.Entidades.AuxiliaresPK;
import com.fenoreste.rest.Entidades.Persona;
import com.fenoreste.rest.Entidades.PersonasPK;
import com.fenoreste.rest.Entidades.Productos;
import com.fenoreste.rest.Entidades.ReferenciasP;
import com.fenoreste.rest.Entidades.V_auxiliares;
import com.fenoreste.rest.Entidades.tipos_cuenta_siscoop;
import com.fenoreste.rest.Entidades.transferencias_completadas_siscoop;
import com.fenoreste.rest.Entidades.v_auxiliaresPK;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import org.xhtmlrenderer.pdf.ITextRenderer;

/**
 *
 * @author Elliot
 */
public abstract class FacadeAccounts<T> {

    private static EntityManagerFactory emf;

    List<Object[]> lista = null;

    public FacadeAccounts(Class<T> entityClass) {
        emf = AbstractFacade.conexion();
    }

    public List<AccountHoldersDTO> validateInternalAccount(String accountId) {
        EntityManager em = emf.createEntityManager();
        int o = Integer.parseInt(accountId.substring(0, 6));
        int p = Integer.parseInt(accountId.substring(6, 11));
        int a = Integer.parseInt(accountId.substring(11, 19));
        System.out.println("AccountIDDDDDDDDDDDDD:"+accountId);
        List<AccountHoldersDTO> holders = new ArrayList<AccountHoldersDTO>();
        try {
            String consulta = "SELECT * FROM auxiliares a WHERE "
                    + "replace(to_char(a.idorigenp,'099999')||to_char(a.idproducto,'09999')||to_char(a.idauxiliar,'09999999'),' ','')='" + accountId+"'";
            System.out.println("consulta:"+consulta);
            Query query = em.createNativeQuery(consulta, Auxiliares.class);
            Auxiliares aa = (Auxiliares) query.getSingleResult();
            PersonasPK personaspk = new PersonasPK(aa.getIdorigen(), aa.getIdgrupo(), aa.getIdsocio());
            Persona person = em.find(Persona.class, personaspk);
            /*
        AuxiliaresPK auxpk = new AuxiliaresPK(o, p, a);
        Auxiliares aa = em.find(Auxiliares.class, auxpk);
       */
        String persona = person.getNombre() + " " + person.getAppaterno() + " " + person.getApmaterno();
            AccountHoldersDTO dto = new AccountHoldersDTO(persona, "SOW");
            holders.add(dto);
        } catch (Exception e) {
            System.out.println("Error al crear lista:" + e.getMessage());
        }
        System.out.println("Holders:" + holders);
        return holders;
    }

    public List<String> validateBeneficiary(String accountId, String accountType) {
        EntityManager em = emf.createEntityManager();
        int o = Integer.parseInt(accountId.substring(0, 6));
        int p = Integer.parseInt(accountId.substring(6, 11));
        int a = Integer.parseInt(accountId.substring(11, 19));
        List<String> lista = new ArrayList<String>();
        try {
            AuxiliaresPK auxpk = new AuxiliaresPK(o, p, a);
            Auxiliares aa = em.find(Auxiliares.class, auxpk);
            PersonasPK personaspk = new PersonasPK(aa.getIdorigen(), aa.getIdgrupo(), aa.getIdsocio());
            Persona person = em.find(Persona.class, personaspk);
            String persona = person.getNombre() + " " + person.getAppaterno() + " " + person.getApmaterno();
            tipos_cuenta_siscoop tps = em.find(tipos_cuenta_siscoop.class, aa.getAuxiliaresPK().getIdproducto());
            if (tps.getProducttypename().replace(" ", "").toUpperCase().contains(accountType.replace(" ", ""))) {
                lista.add(persona.toUpperCase());
                lista.add(tps.getProducttypename().toUpperCase());
                lista.add(accountId);
            } else {
                System.out.println("no es");
            }

        } catch (Exception e) {
            System.out.println("Error al crear lista:" + e.getMessage());
        }
        return lista;
    }

    public String statements(String accountId, String initialDate, String finalDate, int pageSize, int pageStartIndex) {
        EntityManager em = emf.createEntityManager();
        List<StatementsDTO> lista = new ArrayList<StatementsDTO>();
        List<AuxiliaresD> listaa = null;
        File file = null;
        try {
            file = crear_llenar_txt(accountId, initialDate, finalDate);
            //file=new File(ruta()+"e_cuenta_ahorro_0101010011000010667_2.txt");
            System.out.println("fileNameTxt:" + file.getName());
            File fileTxt = new File(ruta() + file.getName());
            if (fileTxt.exists()) {
                File fileHTML = crear_llenar_html(fileTxt, fileTxt.getName().replace(".txt", ".html"));
                if (crearPDF(ruta(), fileHTML.getName())) {
                    String pdf = ruta() + fileHTML.getName().replace(".html", "pdf");
                    fileTxt.delete();
                    fileHTML.delete();
                }
            }
        } catch (Exception e) {
            System.out.println("Error en conver:" + e.getMessage());
        }
        /*try{
        String consulta="SELECT * FROM auxiliares_d WHERE"
                + " replace(to_char(idorigenp,'099999')||to_char(idproducto,'09999')||to_char(idauxiliar,'09999999'),' ','')='"+accountId
                +"' AND date(fecha) BETWEEN '"+initialDate+"' AND '"+finalDate+"'";
        System.out.println("Consulta:"+consulta);
        Query query=em.createNativeQuery(consulta,AuxiliaresD.class);
        query.setFirstResult(pageStartIndex);
        query.setMaxResults(pageSize);
     listaa =query.getResultList();
        for(int i=0;i<listaa.size();i++){
            AuxiliaresD a=listaa.get(i);
            System.out.println("a:"+a);
            StatementsDTO dto=new StatementsDTO(
                    accountId,
                    initialDate,
                    finalDate,
                    String.valueOf(a.getAuxiliaresDPK().getIdorigenp()+a.getAuxiliaresDPK().getIdproducto()+a.getAuxiliaresDPK().getIdauxiliar()),
                    String.valueOf(a.getTransaccion()));    
        lista.add(dto);
        }
        System.out.println("lista:"+lista);
          
    }catch(Exception e){
        System.out.println("Error:"+e.getMessage());
                
    }*/
        System.out.println("count:" + lista.size());
        return file.getName().replace(".txt", ".pdf");

    }

    public List<HoldsDTO> holds(String accountId) {
        EntityManager em = emf.createEntityManager();
        List<HoldsDTO> listaDTO = new ArrayList<HoldsDTO>();
        try {
            String consulta = "SELECT * FROM auxiliares a WHERE"
                    + " replace(to_char(a.idorigenp,'099999')||to_char(a.idproducto,'09999')||to_char(idauxiliar,'09999999'),' ','')='" + accountId
                    + "' AND estatus=2 AND garantia > 0";
            System.out.println("consulta:" + consulta);

            Query query = em.createNativeQuery(consulta, Auxiliares.class);

            List<Auxiliares> lista = query.getResultList();
            System.out.println("listaSize:" + lista.size());
            for (int i = 0; i < lista.size(); i++) {
                System.out.println("ne");
                Auxiliares a = lista.get(i);
                try {
                    String consulta2 = "SELECT * FROM referenciasp WHERE idorigenpr=" + a.getAuxiliaresPK().getIdorigenp() + " AND idproductor=" + a.getAuxiliaresPK().getIdproducto() + " AND idauxiliarr=" + a.getAuxiliaresPK().getIdauxiliar();
                    System.out.println("consulta2:" + consulta2);
                    Query query1 = em.createNativeQuery(consulta2);
                    List<Object[]> listarf = query1.getResultList();
                    String fbloqueo = "";
                    Productos pr = em.find(Productos.class, a.getAuxiliaresPK().getIdproducto());
                    System.out.println("io");
                    System.out.println("lista:" + listarf);
                    for (Object[] obj : listarf) {
                        v_auxiliaresPK auxpk = new v_auxiliaresPK(Integer.parseInt(obj[0].toString()), Integer.parseInt(obj[1].toString()), Integer.parseInt(obj[2].toString()));
                        V_auxiliares aa = em.find(V_auxiliares.class, auxpk);
                        System.out.println("aaa:" + aa.getAuxiliaresPK().getIdproducto());
                        if (aa.getEstatus() <= 2) {
                            System.out.println("entro");
                            if (aa.getEstatus() == 2) {
                                System.out.println("fecha:" + aa.getFechaactivacion());
                                String ff = "2021-03-30 00:00:00";
                                Timestamp tss = Timestamp.valueOf(ff);
                                System.out.println("cadena:" + tss);
                                fbloqueo = String.valueOf(aa.getFechaactivacion());
                            } else if (aa.getEstatus() < 2) {
                                System.out.println("salio del if");
                                fbloqueo = String.valueOf(aa.getFechaape());
                            }
                            HoldsDTO dto = new HoldsDTO(accountId,
                                    Double.parseDouble(a.getGarantia().toString()),
                                    fbloqueo,
                                    pr.getNombre());
                            System.out.println("DTO:" + dto);
                            listaDTO.add(dto);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error:" + e.getMessage());
                }

            }
        } catch (Exception e) {
            System.out.println("Error en holds:" + e.getMessage());
        }
        return listaDTO;
    }

    public Date stringToDate1(String cadena) {
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

    public List<transferencias_completadas_siscoop> History(String accountId, String initialDate, String finalDate, int pageSize, int pageStartIndex) {
        EntityManager em = emf.createEntityManager();
        List<transferencias_completadas_siscoop> lista = null;
        List<transferencias_completadas_siscoop> trs=new ArrayList<transferencias_completadas_siscoop>();
        try {
            String consulta = "SELECT * FROM e_transferenciassiscoop WHERE "
                    + "cuentaorigen='" + accountId.trim()
                    + "' or cuentadestino='" + accountId.trim()
                    + "' AND fechaejecucion BETWEEN '" + initialDate.trim() + "' AND '" + finalDate.trim() + "'";
            System.out.println("Consulta:" + consulta);
            Query query = em.createNativeQuery(consulta, transferencias_completadas_siscoop.class);
            trs=query.getResultList();                       
            query.setFirstResult(pageStartIndex);
            query.setMaxResults(pageSize);
            //lista = query.getResultList();

        } catch (Exception e) {
            System.out.println("Error al leer transacciones:" + e.getMessage());
        }
        return trs;

    }

    public DetailsAccountDTO detailsAccount(String accountId) {
        EntityManager em = emf.createEntityManager();
        DetailsAccountDTO dto = null;
        try {
            String consulta = "SELECT * FROM auxiliares a WHERE replace(to_char(a.idorigenp,'099999')||to_char(a.idproducto,'09999')||to_char(a.idauxiliar,'09999999'),' ','')='" + accountId.trim() + "'";
            System.out.println("Consulta:" + consulta);
            Query query = em.createNativeQuery(consulta, Auxiliares.class);
            Auxiliares a = (Auxiliares) query.getSingleResult();
            System.out.println("a:" + a);
            tipos_cuenta_siscoop tps = em.find(tipos_cuenta_siscoop.class, a.getAuxiliaresPK().getIdproducto());
            System.out.println("tps:" + tps);
            String e = "";
            if (a.getEstatus() == 0) {
                e = "INACTIVE";
            } else if (a.getEstatus() == 1) {
                e = "DORMANT";
            } else if (a.getEstatus() == 2) {
                e = "ACTIVE";
            } else if (a.getEstatus() == 3) {
                e = "CLOSED";
            }
            Query query1 = em.createNativeQuery("SELECT nombre FROM origenes WHERE idorigen=" + a.getAuxiliaresPK().getIdorigenp());
            String sucursal = (String) query1.getSingleResult();
            System.out.println("Sucursal:" + sucursal);
            String aa = String.format("%08d", a.getAuxiliaresPK().getIdauxiliar());
            String cadenaa = aa.substring(4, 8);
            String cade = "******" + cadenaa;
            dto = new DetailsAccountDTO(
                    accountId,
                    String.valueOf(a.getAuxiliaresPK().getIdorigenp()) + "" + String.valueOf(a.getAuxiliaresPK().getIdproducto()) + "" + String.valueOf(a.getAuxiliaresPK().getIdauxiliar()),
                    String.valueOf(a.getAuxiliaresPK().getIdorigenp()) + "" + String.valueOf(a.getAuxiliaresPK().getIdproducto()) + "" + String.valueOf(a.getAuxiliaresPK().getIdauxiliar()),
                    tps.getProducttypename().toUpperCase(),
                    "MXN",
                    String.valueOf(a.getAuxiliaresPK().getIdproducto()),
                    e,
                    sucursal,
                    String.valueOf(a.getFechaactivacion()));
        } catch (Exception e) {
            System.out.println("Error en buscar detalles de cuenta:" + e.getMessage());
        }
        return dto;
    }

    public String Holders(String accountId) {
        EntityManager em = emf.createEntityManager();
        String nombre = "";
        try {
            String consulta = "SELECT * FROM auxiliares a WHERE replace(to_char(a.idorigenp,'099999')||to_char(a.idproducto,'09999')||to_char(a.idauxiliar,'09999999'),' ','')='" + accountId.trim() + "'";
            Query query = em.createNativeQuery(consulta, Auxiliares.class);
            Auxiliares a = (Auxiliares) query.getSingleResult();
            PersonasPK pk = new PersonasPK(a.getIdorigen(), a.getIdgrupo(), a.getIdsocio());
            Persona p = em.find(Persona.class, pk);
            System.out.println("p:" + p);
            nombre = p.getNombre() + " " + p.getAppaterno() + " " + p.getApmaterno();
        } catch (Exception e) {
            System.out.println("Error al buscar propietario de la cuenta:" + e.getMessage());
        }
        return nombre;
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

    public String accountType(int idproducto) {
        EntityManager em = emf.createEntityManager();
        tipos_cuenta_siscoop tip = null;
        try {
            tip = em.find(tipos_cuenta_siscoop.class, idproducto);
        } catch (Exception e) {
            System.out.println("Error al buscar tipo cuenta siscoop:" + e.getMessage());
        }
        return tip.getProducttypename();
    }


    /*Creando estados de cuenta*/
    public static String ruta() {
        String home = System.getProperty("user.home");
        String separador = System.getProperty("file.separator");
        return home + separador + "Banca" + separador;
    }

    public File crear_llenar_txt(String opa, String initialDate, String finalDate) {
        int numeroAleatorio = (int) (Math.random() * 9 + 1);
        String nombre_txt = "e_cuenta_ahorro_" + opa + "_" + String.valueOf(numeroAleatorio) + ".txt";
        System.out.println("nombreTxt:" + nombre_txt);
        EntityManager em = emf.createEntityManager();
        File file = null;
        try {
            String o = opa.substring(0, 6);
            String p = opa.substring(6, 11);
            String a = opa.substring(11, 19);
            String fichero_txt = ruta() + nombre_txt;
            String contenido;
            String consulta = "SELECT sai_estado_cuenta_ahorros(" + o + "," + p + "," + a + ",'" + initialDate + "','" + finalDate + "')";
            System.out.println("Consulta Statements:" + consulta);
            Query query = em.createNativeQuery(consulta);
            contenido = String.valueOf(query.getSingleResult());
            file = new File(fichero_txt);
            // Si el archivo no existe es creado
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(fw);

            bw.write(contenido);
            bw.close();

        } catch (Exception e) {
            em.close();
            System.out.println("Error:" + e.getMessage());
        }
        em.close();
        return file;
    }

    public File crear_llenar_html(File file, String nombre) throws FileNotFoundException {
        String nombre_html = nombre;//=nombre_txt.replace(".txt",".html");
        String html = ruta() + nombre_html;
        File fi = new File(html);
        FileOutputStream fs = new FileOutputStream(fi);
        OutputStreamWriter out = new OutputStreamWriter(fs);
        try {
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.contains("/usr/local/saicoop/img_estado_cuenta_ahorros/")) {
                    String cade = ruta();
                    System.out.println("Cade:" + cade.replace("\\", "/"));
                    linea = linea.replace("/usr/local/saicoop/img_estado_cuenta_ahorros/", cade.replace("\\", "/"));
                }
                if (linea.contains(" & ")) {
                    System.out.println("si tele");
                    linea = linea.replace(" & ", " y ");
                }
                out.write(linea);

            }
            out.close();
        } catch (Exception e) {
            System.out.println("Excepcion leyendo txt" + ": " + e.getMessage());
        }
        return fi;
    }

    public boolean crearPDF(String ruta, String nombreDelHTMLAConvertir) {
        try {
            //ruta donde esta el html a convertir
            String ficheroHTML = ruta + nombreDelHTMLAConvertir;

            String url = new File(ficheroHTML).toURI().toURL().toString();
            System.out.println("url:" + url);
            //ruta donde se almacenara el pdf y que nombre se le data
            String ficheroPDF = ruta + nombreDelHTMLAConvertir.replace(".html", ".pdf");
            OutputStream os = new FileOutputStream(ficheroPDF);
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocument(url);
            renderer.layout();
            renderer.createPDF(os);
            os.close();
            return true;
        } catch (Exception e) {
            System.out.println("Error al crear PDF:" + e.getMessage());
            return false;
        }

    }

    public boolean validarCuenta(String accountId) {
        EntityManager em = emf.createEntityManager();
        try {
            String consulta = "SELECT count(*) FROM auxiliares a WHERE replace(to_char(a.idorigenp,'099999')||to_char(a.idproducto,'09999')||to_char(a.idauxiliar,'09999999'),' ','')='" + accountId.trim() + "'";
            Query query = em.createNativeQuery(consulta);
            int count = Integer.parseInt(String.valueOf(query.getSingleResult()));
            if (count > 0) {
                return true;
            }

        } catch (Exception e) {
            em.clear();
            em.close();
            System.out.println("Error al validar cuenta:" + e.getMessage());
        }
        em.close();
        return false;

    }

    public void cerrar() {
        emf.close();
    }
}
