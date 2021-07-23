/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fenoreste.rest.services;

import com.fenoreste.rest.Auth.Security;
import com.github.cliftonlabs.json_simple.JsonObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author wilmer
 */
@Path("/api/currentbusinessdate")
public class CurrentBusinessDate {
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response BussinesDate(@HeaderParam("authorization") String authString) throws ParseException {
          Security scr = new Security();
        if (!scr.isUserAuthenticated(authString)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        Calendar c1 = Calendar.getInstance();
        String dia = Integer.toString(c1.get(5));
        String mes = Integer.toString(c1.get(2) + 1);
        String annio = Integer.toString(c1.get(1));
        if(dia.length()==1 ){
            dia="0"+dia;
        }
        if(mes.length()==1 ){
            mes="0"+mes;
        }
        String BusinessDate = annio+ "-" + mes + "-" + dia;
        JsonObject json = new JsonObject();
        json.put("currentBusinessDate", BusinessDate);
        return Response.status(Response.Status.OK).entity(json).build();
    }  
    

}
