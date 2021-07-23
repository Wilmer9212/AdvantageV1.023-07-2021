package com.fenoreste.rest.Dao;

import com.fenoreste.rest.ResponseDTO.CustomerAccountDTO;
import com.fenoreste.rest.ResponseDTO.CustomerContactDetailsDTO;
import com.fenoreste.rest.ResponseDTO.CustomerDetailsDTO;
import com.fenoreste.rest.ResponseDTO.CustomerSearchDTO;
import com.fenoreste.rest.Util.AbstractFacade;
import com.fenoreste.rest.Entidades.Auxiliares;
import com.fenoreste.rest.Entidades.AuxiliaresD;
import com.fenoreste.rest.Entidades.AuxiliaresPK;
import com.fenoreste.rest.Entidades.CuentasSiscoop;
import com.fenoreste.rest.Entidades.Persona;
import com.fenoreste.rest.Entidades.PersonasPK;
import com.fenoreste.rest.Entidades.Productos;
import com.fenoreste.rest.Entidades.validaciones_telefono_siscoop;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

import java.util.ArrayList;
import java.util.Date;
import javax.persistence.EntityTransaction;

public abstract class FacadeCustomer<T> {

    private static EntityManagerFactory emf;

    List<Object[]> lista = null;

    public FacadeCustomer(Class<T> entityClass) {
        emf = AbstractFacade.conexion();
    }

    public List<CustomerSearchDTO> search(String ogs) {
        EntityManager em = emf.createEntityManager();
        List<CustomerSearchDTO> listaC = new ArrayList<CustomerSearchDTO>();
        CustomerSearchDTO client = null;
        try {
            int o = Integer.parseInt(ogs.substring(0, 6));
            int g = Integer.parseInt(ogs.substring(6, 8));
            int s = Integer.parseInt(ogs.substring(8, 14));
            System.out.println("ogs:" + o + "" + g + "" + s);
            PersonasPK pk = new PersonasPK(o, g, s);
            Persona p = em.find(Persona.class, pk);

            String customerId = ogs;
            String name = "", curp = "", taxId = "", customerType = "";
            Date birthDate = null;
            name = p.getNombre() + " " + p.getAppaterno() + " " + p.getApmaterno();
            taxId = p.getCurp();
            birthDate = p.getFechanacimiento();
            if (p.getRazonSocial() == null) {
                customerType = "individual";
            } else {
                customerType = "grupal";
            }

           client = new CustomerSearchDTO(
                    customerId,
                    name,
                    taxId,
                    dateToString(birthDate).replace("/","-"),
                    "individual");
            listaC.add(client);

            return listaC;
        } catch (Exception e) {
            em.close();
            System.out.println("Error al buscar cliente:" + e.getMessage());
           
        }
         em.close();
        return null;
    }

    public CustomerDetailsDTO details(String ogs) {
        EntityManager em = emf.createEntityManager();
        List<CustomerDetailsDTO> listaC = new ArrayList<CustomerDetailsDTO>();
        CustomerDetailsDTO client = null;
        try {
            int o = Integer.parseInt(ogs.substring(0, 6));
            int g = Integer.parseInt(ogs.substring(6, 8));
            int s = Integer.parseInt(ogs.substring(8, 14));
            PersonasPK pk = new PersonasPK(o, g, s);
            Persona p = em.find(Persona.class, pk);
            String customerId = ogs;
            String name = "", customerType = "";
            name = p.getNombre() + " " + p.getAppaterno() + " " + p.getApmaterno();
            if (p.getRazonSocial() == null) {
                customerType = "individual";
            } else {
                customerType = "grupal";
            }
            client = new CustomerDetailsDTO(
                    customerId,
                    name,
                    "individual");
            return client;
        } catch (Exception e) {
            System.out.println("Error al buscar cliente:" + e.getMessage());
            em.close();
        }
        em.close();
    
        return null;
    }

    public List<CustomerContactDetailsDTO> ContactDetails(String ogs) {
        EntityManager em = emf.createEntityManager();
        Query query = null;
        List<Object[]> ListaObjetos = null;
        String consulta = "SELECT CASE WHEN p.telefono != '' THEN p.telefono ELSE '0' END as phone,"
                + " CASE WHEN p.celular != '' THEN p.celular ELSE '0' END as cellphone,"
                + " CASE WHEN p.email != '' THEN  p.email ELSE '0' END as email FROM personas p WHERE replace(to_char(p.idorigen,'099999')||to_char(p.idgrupo,'09')||to_char(p.idsocio,'099999'),' ','')='" + ogs + "'";
        CustomerContactDetailsDTO contactsPhone = new CustomerContactDetailsDTO();
        CustomerContactDetailsDTO contactsCellphone = new CustomerContactDetailsDTO();
        CustomerContactDetailsDTO contactsEmail = new CustomerContactDetailsDTO();
        List<CustomerContactDetailsDTO> ListaContactos = new ArrayList<CustomerContactDetailsDTO>();

        try {
            int o = Integer.parseInt(ogs.substring(0, 6));
            int g = Integer.parseInt(ogs.substring(6, 8));
            int s = Integer.parseInt(ogs.substring(8, 14));
            PersonasPK pk = new PersonasPK(o, g, s);
            Persona p = em.find(Persona.class, pk);
            if (p.getTelefono() != null) {
                contactsPhone.setCustomerContactId(ogs);
                contactsPhone.setCustomerContactType("phone");
                contactsPhone.setPhoneNumber("521"+p.getTelefono());
                ListaContactos.add(contactsPhone);

            }
            if (p.getCelular() != null) {
                contactsCellphone.setCustomerContactId(ogs);
                contactsCellphone.setCustomerContactType("phone");
                contactsCellphone.setCellphoneNumber(p.getCelular());
                ListaContactos.add(contactsCellphone);
            }
            if (p.getEmail() != null) {
                contactsEmail.setCustomerContactId(ogs);
                contactsEmail.setCustomerContactType("email");
                contactsEmail.setEmail(p.getEmail());

                ListaContactos.add(contactsEmail);
            }
        } catch (Exception e) {
            em.close();
            System.out.println("Error al obtener detalles del socio:" + e.getMessage());
           
        }
        em.close();

        return ListaContactos;
    }

    public List<CustomerAccountDTO> Accounts(String customerId) {
        EntityManager em = emf.createEntityManager();
        Query query = null;
        List<CustomerAccountDTO> ListaProductos = new ArrayList<CustomerAccountDTO>();
        String consulta = "SELECT * FROM auxiliares a INNER JOIN tipos_cuenta_siscoop tp USING(idproducto) WHERE replace((to_char(idorigen,'099999')||to_char(idgrupo,'09')||to_char(idsocio,'099999')),' ','')='" + customerId + "' AND estatus=2";
        CustomerAccountDTO producto = new CustomerAccountDTO();
        try {
            query = em.createNativeQuery(consulta, Auxiliares.class);
            List<Auxiliares> ListaProd = query.getResultList();
            String status = "";
            String accountType = "";
            Object[] arr = {};
            Object[] arr1 = {"relationCode", "SOW"};
            List<CustomerAccountDTO> listaDeCuentas = new ArrayList<CustomerAccountDTO>();
            
            for (int k = 0; k < 1; k++) {
                for (int i = 0; i < ListaProd.size(); i++) {
                    Auxiliares a = ListaProd.get(i);
                    System.out.println("IdproductoA:" + a.getAuxiliaresPK().getIdproducto());
                    try {
                        CuentasSiscoop tp = em.find(CuentasSiscoop.class, a.getAuxiliaresPK().getIdproducto());
                        accountType = String.valueOf(tp.getProducttypename().toUpperCase());
                        if(accountType.contains("TIME")){
                            accountType="TIME";
                        }
                    } catch (Exception e) {
                        System.out.println("Error producido:"+e.getMessage());
                    }
                    if (a.getEstatus() == 2) {
                        status = "OPEN";
                    } else if (a.getEstatus() == 3) {
                        status = "CLOSED";
                    } else {
                        status = "INACTIVE";
                    }

                    String og = String.format("%06d", a.getIdorigen()) + String.format("%02d", a.getIdgrupo());
                    String s = String.format("%06d", a.getIdsocio());

                    String op = String.format("%06d", a.getAuxiliaresPK().getIdorigenp()) + String.format("%05d", a.getAuxiliaresPK().getIdproducto());
                    String aa = String.format("%08d", a.getAuxiliaresPK().getIdauxiliar());
                    System.out.println("opa:"+op+","+aa);
                    String cadenaa=aa.substring(4, 8);
                    String cade="******"+cadenaa;
                    
                    producto = new CustomerAccountDTO(
                            op + aa,
                            op + aa,
                            cade,
                            accountType,
                            "MXN",
                            String.valueOf(a.getAuxiliaresPK().getIdproducto().toString()),
                            status,
                            arr,
                            arr1);
                    listaDeCuentas.add(producto);
                    accountType = "";
                }
            }
            return listaDeCuentas;

        } catch (Exception e) {
            em.close();
            System.out.println("Error al obtener cuentas:" + e.getMessage());
        }
        em.close();
        return null;
    }

    public boolean findCustomer(String ogs) {
        boolean bandera = false;
        EntityManager em = emf.createEntityManager();
        try {
            Query query = em.createNativeQuery("SELECT * FROM personas WHERE replace(to_char(p.idorigen,'099999')||to_char(p.idgrupo,'09')||to_char(p.idsocio,'099999'),' ','')='" + ogs + "'");
            if (query != null) {
                bandera = true;
            }
        } catch (Exception e) {
            cerrar();
        }
        return bandera;
    }

    public String validateSetContactDetails(String customerId, String phone1) {
        EntityManager em = emf.createEntityManager();
        String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
        String CHAR_UPPER = CHAR_LOWER.toUpperCase();
        String NUMBER = "0123456789";

        String DATA_FOR_RANDOM_STRING = CHAR_LOWER + CHAR_UPPER + NUMBER;
        SecureRandom random = new SecureRandom();
        char rndChar = 0;
        String cadena = "";
        for (int i = 0; i < 12; i++) {
            // 0-62 (exclusivo), retorno aleatorio 0-61
            int rndCharAt = random.nextInt(DATA_FOR_RANDOM_STRING.length());
            rndChar = DATA_FOR_RANDOM_STRING.charAt(rndCharAt);
            String c = String.valueOf(rndChar);
            cadena = cadena + c;
        }

        try {

             EntityTransaction tr=em.getTransaction();
            tr.begin();
            em.createNativeQuery("UPDATE personas p SET celular='"+phone1.replace("+","")+
                                "' WHERE replace(to_char(p.idorigen,'099999')||to_char(p.idgrupo,'09')||to_char(p.idsocio,'099999'),' ','')='" + customerId + "'").executeUpdate();
            
            validaciones_telefono_siscoop vl=new validaciones_telefono_siscoop(0,cadena.toUpperCase(),customerId,phone1);
            em.persist(vl);
            tr.commit();
        } catch (Exception e) {
            System.out.println("Error en validar datos");
        }

        return cadena;
    }

    public String executeSetContactDetails(String validationId) {
        EntityManager em = emf.createEntityManager();
        String estatus = "";
        try {
            String consulta = "SELECT * FROM validaciones_telefonos_siscoop WHERE validacion='" + validationId + "'";
            Query query = em.createNativeQuery(consulta, validaciones_telefono_siscoop.class);
            validaciones_telefono_siscoop dto = (validaciones_telefono_siscoop) query.getSingleResult();
            EntityTransaction tr = em.getTransaction();
            tr.begin();
            int o = 0, p = 0, a = 0;
            o = Integer.parseInt(dto.getCustomerid().substring(0, 6));
            p = Integer.parseInt(dto.getCustomerid().substring(6, 8));
            a = Integer.parseInt(dto.getCustomerid().substring(8, 14));

            PersonasPK pk = new PersonasPK(o, p, a);
            Persona pp = em.find(Persona.class, pk);
            pp.setCelular(dto.getSettelefono());
            em.persist(pp);
            tr.commit();

            if (pp.getCelular().equals(dto.getSettelefono())) {
                estatus = "completed";
            } else {
                estatus = "incompleted";
            }
        } catch (Exception e) {
            System.out.println("Error:" + e.getMessage());
        }

        return estatus;
    }

    public Double[] position(String customerId) {
        EntityManager em = emf.createEntityManager();
        Double ledGer=0.0,avalaible=0.0;
        double saldo_congelado_ahorros=0.0;
                double saldo_disponible_ahorros=0.0;
        try {
            String c = "SELECT * FROM auxiliares a"
                    + " INNER JOIN productos pr USING(idproducto)"
                    + " WHERE pr.tipoproducto in(0,1) AND replace(to_char(a.idorigen,'099999')||"
                    + "to_char(a.idgrupo,'09')||to_char(a.idsocio,'099999'),' ','')='" + customerId.trim() + "'";
            System.out.println("Consulta:"+c);
            Query query = em.createNativeQuery(c, Auxiliares.class);
            List<Auxiliares> lista = query.getResultList();
            Double saldoAhorro = 0.0;
            Double saldoDPF=0.0;
            
            Double saldoGarantia=0.0;
            
            for (int i = 0; i < lista.size(); i++) {
                Auxiliares a = lista.get(i);
                Productos pr = em.find(Productos.class, a.getAuxiliaresPK().getIdproducto());
                Double garantiaDPF=0.0;
                Double saldoAvalaibleDPF=0.0;
                Double saldoLedgerDPF=0.0;
                Double garantiaAhorro=0.0;
                Double saldoAvalaibleAhorro=0.0;
                Double saldoLedgerAhorro=0.0;
                
                 double saldo_congelado=0.0;
                    double saldo_disponible=0.0;
                if (pr.getTipoproducto() == 1) {
                    System.out.println("Entroooo:");
                    System.out.println("Es una inversion");
                    //Se suma fechaactivacion mas plazos para determinar si el producto ya se puede cobrar o aun no
                    String cc = "SELECT a.fechaactivacion + " + Integer.parseInt(String.valueOf(a.getPlazo())) + " FROM auxiliares a WHERE a.idorigenp="
                            + a.getAuxiliaresPK().getIdorigenp()
                            + " AND a.idproducto=" + a.getAuxiliaresPK().getIdproducto()
                            + " AND a.idauxiliar=" + a.getAuxiliaresPK().getIdauxiliar();
                    Query query1 = em.createNativeQuery(cc);
                    String fecha = String.valueOf(query1.getSingleResult()).replace("-", "/");
                    Date hoy = new Date();
                    Date fa = stringToDate(fecha);//fecha obtenida
                    //si la fecha obtenida es igual al dia actual(hoy) o esta antes el dpf ya se puede retirar siempre y cuando no este amparando credito
                 saldoLedgerDPF=saldoLedgerDPF+Double.parseDouble(a.getSaldo().toString());
                    if (fa == hoy || fa.before(hoy)){
                        //si el dpf tiene amparado credito
                        if(Integer.parseInt(a.getGarantia().toString())>0){
                          garantiaDPF=garantiaDPF+Double.parseDouble(a.getGarantia().toString());                        
                          saldoAvalaibleDPF=saldoAvalaibleDPF+(Double.parseDouble(a.getSaldo().toString())-garantiaDPF);  
                        }else{
                          saldoAvalaibleDPF=Double.parseDouble(a.getSaldo().toString());
                        }       
                 //Si el dpf aun no se puede retirar
                }
                  
                }else if(pr.getTipoproducto()==0){
                double saldodis=Double.parseDouble(a.getSaldo().toString());
                double saldoblok=0.0;
                if(Double.parseDouble(a.getGarantia().toString())>0){    
                   double garantia=Double.parseDouble(a.getGarantia().toString());
                    System.out.println("Garantia:"+garantia);
                saldo_congelado=saldo_congelado+garantia;
                }else{
                saldo_disponible=saldo_disponible+saldodis;
                }
              }
              saldo_congelado_ahorros=saldo_congelado_ahorros+saldo_congelado;
              saldo_disponible_ahorros=saldo_disponible_ahorros+saldo_disponible;
              
            }          
           
           } catch (Exception e) {
             e.getStackTrace();
            System.out.println("Error:" + e.getMessage());
        }
        Double saldos[]=new Double[2];
        saldos[0]=saldo_congelado_ahorros;
        saldos[1]=saldo_disponible_ahorros;
        System.out.println("Saldos:"+saldos[1]);
        return saldos;
    }
    
    public Double[] positionHistory(String customerId,String fecha1,String fecha2) {
        EntityManager em = emf.createEntityManager();
        Double ledGer=0.0,avalaible=0.0;
        try {
            String c = "SELECT * FROM auxiliares_d ad "
                    + "INNER JOIN auxiliares a using(idorigenp,idproducto,idauxiliar)"
                    + " INNER JOIN productos pr USING(idproducto)"
                    + " WHERE pr.tipoproducto in(0,1) AND replace(to_char(a.idorigen,'099999')||"
                    + "to_char(a.idgrupo,'09')||to_char(a.idsocio,'099999'),' ','')='" + customerId.trim() 
                    + "' AND date(ad.fecha) between '"+fecha1+"' AND '"+fecha2+"'";
            System.out.println("Consulta:"+c);
            Query query = em.createNativeQuery(c, AuxiliaresD.class);
            List<AuxiliaresD> lista = query.getResultList();
           
            for (int i = 0; i < lista.size(); i++) {
                AuxiliaresD ad = lista.get(i);
                AuxiliaresPK apk=new AuxiliaresPK(ad.getAuxiliaresDPK().getIdorigenp(),ad.getAuxiliaresDPK().getIdproducto(),ad.getAuxiliaresDPK().getIdauxiliar());
                Auxiliares a=em.find(Auxiliares.class,apk);
                System.out.println("paso");
                Productos pr = em.find(Productos.class, a.getAuxiliaresPK().getIdproducto());
                Double garantiaDPF=0.0;
                Double saldoAvalaibleDPF=0.0;
                Double saldoLedgerDPF=0.0;
                Double garantiaAhorro=0.0;
                Double saldoAvalaibleAhorro=0.0;
                Double saldoLedgerAhorro=0.0;
                if (pr.getTipoproducto() == 1) {
                    //Se suma fechaactivacion mas plazos para determinar si el producto ya se puede cobrar o aun no
                    String cc = "SELECT a.fechaactivacion + " + Integer.parseInt(String.valueOf(a.getPlazo())) + " FROM auxiliares a WHERE a.idorigenp="
                            + a.getAuxiliaresPK().getIdorigenp()
                            + " AND a.idproducto=" + a.getAuxiliaresPK().getIdproducto()
                            + " AND a.idauxiliar=" + a.getAuxiliaresPK().getIdauxiliar();
                    Query query1 = em.createNativeQuery(cc);
                    String fecha = String.valueOf(query1.getSingleResult()).replace("-", "/");
                    Date hoy = new Date();
                    Date fa = stringToDate(fecha);//fecha obtenida
                    //si la fecha obtenida es igual al dia actual(hoy) o esta antes el dpf ya se puede retirar siempre y cuando no este amparando credito
                 saldoLedgerDPF=saldoLedgerDPF+Double.parseDouble(a.getSaldo().toString());
                    if (fa == hoy || fa.before(hoy)){
                        //si el dpf tiene amparado credito
                        if(Integer.parseInt(a.getGarantia().toString())>0){
                          garantiaDPF=garantiaDPF+Double.parseDouble(a.getGarantia().toString());                        
                          saldoAvalaibleDPF=saldoAvalaibleDPF+(Double.parseDouble(a.getSaldo().toString())-garantiaDPF);  
                        }else{
                          saldoAvalaibleDPF=Double.parseDouble(a.getSaldo().toString());
                        }       
                 //Si el dpf aun no se puede retirar
                }
                  
                }else if(pr.getTipoproducto()==0){
                saldoLedgerAhorro=saldoLedgerAhorro+Double.parseDouble(a.getSaldo().toString());
                if(Double.parseDouble(a.getGarantia().toString())>0){                        
                 garantiaAhorro= garantiaAhorro+Double.parseDouble(a.getGarantia().toString());
                 saldoAvalaibleAhorro=saldoAvalaibleAhorro+ (Double.parseDouble(a.getSaldo().toString())-garantiaAhorro);
                }else{
                 saldoAvalaibleAhorro=saldoAvalaibleAhorro+Double.parseDouble(a.getSaldo().toString());
                }
              }
              ledGer=ledGer+(saldoLedgerDPF+saldoLedgerAhorro);
              avalaible=avalaible+(saldoAvalaibleDPF+saldoAvalaibleAhorro);
            }          
           
           } catch (Exception e) {
             e.getStackTrace();
            System.out.println("Error:" + e.getMessage());
        }
        Double saldos[]=new Double[2];
        saldos[0]=ledGer;
        saldos[1]=avalaible;
        System.out.println("Saldos:"+saldos[1]);
        return saldos;
    }
    
    
    

    public String dateToString(Date cadena) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        String cadenaStr = sdf.format(cadena);
        return cadenaStr;
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

    public void cerrar() {
        emf.close();
    }

}
