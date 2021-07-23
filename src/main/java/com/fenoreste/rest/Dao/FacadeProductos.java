package com.fenoreste.rest.Dao;
import com.fenoreste.rest.Entidades.Auxiliares;
import com.fenoreste.rest.Entidades.CuentasSiscoop;
import com.fenoreste.rest.Entidades.tipos_cuenta_siscoop;
import com.fenoreste.rest.ResponseDTO.ProductsDTO;
import com.fenoreste.rest.Util.AbstractFacade;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

public abstract class FacadeProductos<T> {
  private static EntityManagerFactory emf;
  
  public FacadeProductos(Class<T> entityClass) {
    emf = AbstractFacade.conexion();
  }
  
  public List<ProductsDTO> getProductos(String accountType) {
    List<ProductsDTO> ListagetP = new ArrayList<>();
    EntityManager em = emf.createEntityManager();
    try {
      String consulta = "";
      if (!accountType.equals("")) {
        consulta = "SELECT * FROM tipos_cuenta_siscoop WHERE UPPER(producttypename)='" + accountType.toUpperCase() + "'";
      } else {
        consulta = "SELECT * FROM tipos_cuenta_siscoop";
      } 
      Query query = em.createNativeQuery(consulta, CuentasSiscoop.class);
      List<CuentasSiscoop> Lista = query.getResultList();
      for (int i = 0; i < Lista.size(); i++) {
        CuentasSiscoop model = Lista.get(i);
        String c = "";
        c = model.getProducttypename();
        if (model.getProducttypename().toUpperCase().contains("TIME"))
          c = "TIME"; 
        ProductsDTO dto = new ProductsDTO(String.valueOf(model.getIdproducto()), model.getProducttypeid(), c, model.getDescripcion().toUpperCase());
        ListagetP.add(dto);
      } 
      System.out.println("ListaProd:" + ListagetP.size());
    } catch (Exception e) {
      em.close();
    } 
    em.close();
    return ListagetP;
  }
  
  public List<String> Rates(String accountType, int amount, String customerId, String productCode) {
    EntityManager em = emf.createEntityManager();
    System.out.println("si llego");
    List<String> listaString = new ArrayList<>();
    try {
      String consulta = "SELECT * FROM auxiliares a INNER JOIN tipos_cuenta_siscoop tps USING(idproducto) WHERE replace(to_char(a.idorigen,'099999')||to_char(a.idgrupo,'09')||to_char(a.idsocio,'099999'),' ','')='" + customerId + "' AND REPLACE(UPPER(tps.producttypename),' ','')='" + accountType.toUpperCase() + "' AND tps.idproducto=" + productCode;
      System.out.println("Consulta:" + consulta);
      Query query = em.createNativeQuery(consulta, Auxiliares.class);
      Auxiliares a = (Auxiliares)query.getSingleResult();
      String ven = "";
      tipos_cuenta_siscoop tps = (tipos_cuenta_siscoop)em.find(tipos_cuenta_siscoop.class, a.getAuxiliaresPK().getIdproducto());
      if (tps.getProducttypeid().intValue() == 3) {
        ven = "SELECT DATE(TRIM(TO_CHAR(DATE(a.fechaactivacion + INT4(a.plazo)),'dd/mm/yyyy'))) FROM auxiliares a WHERE idorigenp=" + a.getAuxiliaresPK().getIdorigenp() + " AND idproducto=" + a.getAuxiliaresPK().getIdproducto() + " AND idauxiliar=" + a.getAuxiliaresPK().getIdauxiliar() + " AND estatus=2";
        System.out.println("consulta2:" + ven);
      } else if (tps.getProducttypeid().intValue() == 5) {
        ven = "SELECT vence FROM amortizaciones WHERE idorigenp=" + a.getAuxiliaresPK().getIdorigenp() + " AND idproducto=" + a.getAuxiliaresPK().getIdproducto() + " AND idauxiliar=" + a.getAuxiliaresPK().getIdauxiliar() + " ORDER BY vence DESC LIMIT 1";
        System.out.println("Consulta 2:" + ven);
      } 
      Query query2 = em.createNativeQuery(ven);
      String fecha = String.valueOf(query2.getSingleResult());
      listaString.add(String.valueOf(Double.parseDouble(String.valueOf(a.getTasaio()))));
      listaString.add(fecha);
      System.out.println("lista:" + listaString);
    } catch (Exception e) {
      System.out.println("Error al buscar tasa de productos:" + e.getMessage());
    } 
    return listaString;
  }
  
   
  public boolean productRates(String accountType, String productCode) {
    EntityManager em = emf.createEntityManager();
      System.out.println("AccountType:"+accountType+",ProductCode:"+productCode);
    try {
      String consulta = "SELECT count(*) FROM auxiliares a INNER JOIN tipos_cuenta_siscoop tps USING(idproducto) WHERE upper(tps.producttypename) LIKE '%" + accountType.toUpperCase() + "%' AND tps.idproducto=" + productCode;
      System.out.println("Consulta:" + consulta);
      Query query = em.createNativeQuery(consulta);
      int c=Integer.parseInt(String.valueOf(query.getSingleResult()));
      if(c>0){
          return true;
      }
      else{
          return false;
      }
    } catch (Exception e) {
      System.out.println("Error al buscar tasa de productos:" + e.getMessage());
    } 
    return false;
  }
  
  
  public void cerrar() {
    emf.close();
  }
}
