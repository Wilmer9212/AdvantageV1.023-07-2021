/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fenoreste.rest.services;

import com.fenoreste.rest.Auth.Security;
import com.fenoreste.rest.Dao.AccountsDAO;
import java.util.Calendar;
import javax.json.Json;
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
@Path("/api/timedeposit")
public class TimeResources {
  
     @POST
    @Path("/create/validate")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response TimeDepositValidateCreate(String cadena, @HeaderParam("authorization") String authString) {
        Security scr = new Security();
        if (!scr.isUserAuthenticated(authString)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        String accountId = "";
        AccountsDAO dao = new AccountsDAO();
        JSONObject jsonRecibido = new JSONObject(cadena);
        try {
            Calendar c1 = Calendar.getInstance();
            String dia = Integer.toString(c1.get(5));
            String mes = Integer.toString(c1.get(2) + 1);
            String annio = Integer.toString(c1.get(1));
            String BusinessDate = String.format("%04d",Integer.parseInt(annio)) +"-" + String.format("%02d",Integer.parseInt(mes)) + "-" + String.format("%02d",Integer.parseInt(dia));
            javax.json.JsonObject found = Json.createObjectBuilder()
                    .add("validationId", "0988888")
                    .add("fees", Json.createArrayBuilder())
                    .add("executionDate", BusinessDate)
                    .add("interestRate", 5.00)
                    .add("maturityDate", BusinessDate)
                    .build();
            return Response.status(Response.Status.OK).entity(found).build();
        } catch (Exception e) {
            System.out.println("Error:" + e.getMessage());
        } finally {
            dao.cerrar();
        }
        return null;
    }
    
    
    @POST
    @Path("/create/execute")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response TimeDepositExecuteCreate(String cadena, @HeaderParam("authorization") String authString) {
        Security scr = new Security();
        if (!scr.isUserAuthenticated(authString)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        String accountId = "";
        AccountsDAO dao = new AccountsDAO();
        JSONObject jsonRecibido = new JSONObject(cadena);
        try {
            Calendar c1 = Calendar.getInstance();
            String dia = Integer.toString(c1.get(5));
            String mes = Integer.toString(c1.get(2) + 1);
            String annio = Integer.toString(c1.get(1));
            String BusinessDate = String.format("%04d",Integer.parseInt(annio)) +"-" + String.format("%02d",Integer.parseInt(mes)) + "-" + String.format("%02d",Integer.parseInt(dia));
            String dt = null;
            javax.json.JsonObject found = Json.createObjectBuilder()
                    .add("status", "completed")
                    .add("executionId", "ABCDEFGHISDFSFS")
                    .add("accountId", "668344374")
                    .add("accountNumber", "6464564645")
                    .add("displayAccountNumber", "345734983")
                    .build();
            return Response.status(Response.Status.OK).entity(found).build();
        } catch (Exception e) {
            System.out.println("Error:" + e.getMessage());
        } finally {
            dao.cerrar();
        }
        return null;
    }
    
      @POST
    @Path("/reedem/validate")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response TimeDepositReedemValidate(String cadena, @HeaderParam("authorization") String authString) {
        Security scr = new Security();
        if (!scr.isUserAuthenticated(authString)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        String accountId = "";
        AccountsDAO dao = new AccountsDAO();
        JSONObject jsonRecibido = new JSONObject(cadena);
        Calendar c1 = Calendar.getInstance();
        String dia = Integer.toString(c1.get(5));
        String mes = Integer.toString(c1.get(2) + 1);
        String annio = Integer.toString(c1.get(1));
        String diaa = dia + "/" + mes + "/" + annio;
        try {
            String dt = null;
            String customerId = "", timeAccountId = "", creditAccountId = "";
            customerId = jsonRecibido.getString("customerId");
            timeAccountId = jsonRecibido.getString("timeAccountId");
            creditAccountId = jsonRecibido.getString("creditAccountId");
            javax.json.JsonObject found = Json.createObjectBuilder()
                    .add("validationId", "76GHJAWERT6V")
                    .add("fees", Json.createArrayBuilder())
                    .add("executionDate", diaa.replace("/","-"))
                    .add("netAmount", Json.createObjectBuilder()
                            .add("amount", 2000)
                            .add("currencyCode", "MXN")
                            .build())
                    .add("principalBalance", Json.createObjectBuilder()
                            .add("amount", 2700)
                            .add("currencyCode", "MXN")
                            .build())
                    .add("interestDue", Json.createObjectBuilder()
                            .add("amount", 10.5)
                            .add("currencyCode", "MXN")
                            .build())
                    .add("interestWithheld", Json.createObjectBuilder()
                            .add("amount", 10.5)
                            .add("currencyCode", "MXN")
                            .build())
                    .build();
            return Response.status(Response.Status.OK).entity(found).build();
        } catch (Exception e) {
            System.out.println("Error:" + e.getMessage());
        } finally {
            dao.cerrar();
        }
        return null;
    }

    @POST
    @Path("/reedem/execute")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response TimeDepositReedemExecute(String cadena, @HeaderParam("authorization") String authString) {
        Security scr = new Security();
        if (!scr.isUserAuthenticated(authString)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        String accountId = "";
        AccountsDAO dao = new AccountsDAO();
        JSONObject jsonRecibido = new JSONObject(cadena);
        Calendar c1 = Calendar.getInstance();
        String dia = Integer.toString(c1.get(5));
        String mes = Integer.toString(c1.get(2) + 1);
        String annio = Integer.toString(c1.get(1));
        String diaa = dia + "/" + mes + "/" + annio;
        try {
            String dt = null;
            String validationId = "";
            validationId = jsonRecibido.getString("validationId");
            javax.json.JsonObject found = Json.createObjectBuilder()
                    .add("status", "completed")
                    .build();
            return Response.status(Response.Status.OK).entity(found).build();
        } catch (Exception e) {
            System.out.println("Error:" + e.getMessage());
        } finally {
            dao.cerrar();
        }
        return null;
    }

}
