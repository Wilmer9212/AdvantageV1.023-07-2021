/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fenoreste.rest.services;

import com.fenoreste.rest.Auth.Security;
import com.github.cliftonlabs.json_simple.JsonObject;
import java.math.BigDecimal;
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
@Path("api/card")
public class CardsResources {
    
    @POST
    @Path("/linkedaccounts")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response linkedAccounts(String cadena,@HeaderParam("authorization") String authString){
     Security scr=new Security();
     if(!scr.isUserAuthenticated(authString)){
            return Response.status(Response.Status.UNAUTHORIZED).build();
     }
        JSONObject jsonRecibido=new JSONObject(cadena);
         javax.json.JsonObject json=null;
        try {
            String cardId=jsonRecibido.getString("cardId");
           json=Json.createObjectBuilder().add("linkedAccounts",Json.createArrayBuilder()
                                          .add(Json.createObjectBuilder().add("accountId","09989845")
                                                                         .add("accountNumber", "098989898")
                                                                         .add("accountType","SAVINGS")
                                                                         .add("currencyCode","MXN")
                                                                         .add("productCode","110")
                                                                         .add("status","OPEN")
                                                                         .add("activeCardChannels", Json.createArrayBuilder()
                                                                                                   .add(Json.createObjectBuilder()
                                                                                                            .add("channelCode", "POS")
                                                                                                            .build()))
                                                                         .build()))
                                          .build();
        } catch (Exception e) {
            System.out.println("Error:"+e.getMessage());
        }
        return Response.status(Response.Status.OK).entity(json).build();
    }
    
    @POST
    @Path("/limits")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response limits(String cadena,@HeaderParam("authorization") String authString){
    Security scr=new Security();
     if(!scr.isUserAuthenticated(authString)){
            return Response.status(Response.Status.UNAUTHORIZED).build();
     }
        JSONObject jsonRecibido=new JSONObject(cadena);
        javax.json.JsonObject json=null;
        try {
            String cardId=jsonRecibido.getString("cardId");
           json=Json.createObjectBuilder().add("warnings",Json.createArrayBuilder()
                                          .add(Json.createObjectBuilder().add("code","W099")
                                                                         .add("message", "No se encontraron limites para la tarjeta")
                                                                         .build()))
                                          .add("limits",Json.createArrayBuilder())
                                          .build();
        } catch (Exception e) {
            System.out.println("Error:"+e.getMessage());
        }
        return Response.status(Response.Status.OK).entity(json).build();
    } 
}
