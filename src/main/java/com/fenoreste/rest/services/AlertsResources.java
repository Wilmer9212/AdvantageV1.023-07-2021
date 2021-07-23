
package com.fenoreste.rest.services;

import com.fenoreste.rest.Auth.Security;
import com.fenoreste.rest.Dao.AlertsDAO;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.JSONObject;

/**
 *
 * @author Elliot
 */
@Path("api/alert")
public class AlertsResources {
    
    @POST    
    @Path("/subscription/set/validate")
    @Produces({MediaType.APPLICATION_JSON+ ";charset=utf-8"})
    @Consumes({MediaType.APPLICATION_JSON + ";charset=utf-8"})
    public Response alertValidate(String cadena,@HeaderParam("authorization") String authString){
    
    Security scr=new Security();
     if(!scr.isUserAuthenticated(authString)){
            return Response.status(Response.Status.UNAUTHORIZED).build();
     }
        JSONObject jsonRecibido=new JSONObject(cadena);
        System.out.println("JsonSubscriptionValidate:"+jsonRecibido);
        AlertsDAO dao=new AlertsDAO();
        try{
         String customerId=jsonRecibido.getString("customerId");
         String code=jsonRecibido.getString("alertCode");
         boolean enabled=jsonRecibido.getBoolean("enabled"); 
         String validationId=dao.validateAlert(customerId, code, enabled);
        
         com.github.cliftonlabs.json_simple.JsonObject json=new com.github.cliftonlabs.json_simple.JsonObject();
         json.put("validationId",validationId);
         json.put("fees",null);
         json.put("effectiveDate",null);
         return Response.status(Response.Status.OK).entity(json).build();
        }catch(Exception e){
            System.out.println("Error al convertir json:"+e.getMessage());   
        }finally{
            dao.cerrar();
        }
        return null;
    }
    
    @POST    
    @Path("/subscription/set/execute")
    @Produces({MediaType.APPLICATION_JSON+ ";charset=utf-8"})
    @Consumes({MediaType.APPLICATION_JSON + ";charset=utf-8"})
    public Response alertExecute(String cadena,@HeaderParam("authorization") String authString){
    Security scr=new Security();
     if(!scr.isUserAuthenticated(authString)){
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        JSONObject jsonRecibido=new JSONObject(cadena);
        System.out.println("SubscriptionAlertExecute:"+jsonRecibido);
        AlertsDAO dao=new AlertsDAO();
        try{
         String validationId=jsonRecibido.getString("validationId"); 
         String estatus=dao.executeAlert(validationId);
         com.github.cliftonlabs.json_simple.JsonObject json=new com.github.cliftonlabs.json_simple.JsonObject();
         json.put("status",estatus);
         return Response.status(Response.Status.OK).entity(json).build();
        }catch(Exception e){
            System.out.println("Error al convertir json:"+e.getMessage());   
        }finally{
            dao.cerrar();
        }
        return null;
    }
}
