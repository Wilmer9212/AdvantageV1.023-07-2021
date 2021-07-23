package com.fenoreste.rest.services;

import com.fenoreste.rest.Auth.Security;
import com.fenoreste.rest.ResponseDTO.AccountHoldersDTO;
import com.fenoreste.rest.ResponseDTO.DetailsAccountDTO;
import com.fenoreste.rest.ResponseDTO.HoldsDTO;
import com.fenoreste.rest.Dao.AccountsDAO;
import com.fenoreste.rest.Dao.TransfersDAO;
import com.fenoreste.rest.Entidades.transferencias_completadas_siscoop;
import com.github.cliftonlabs.json_simple.JsonObject;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.List;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonValue;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.codehaus.jettison.json.JSONException;
import org.json.JSONArray;
import org.json.JSONObject;

@Path("api/account")
public class AccountsResources {

    @POST
    @Path("/holders")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response accountHolders(String cadena, @HeaderParam("authorization") String authString) throws JSONException {
         Security scr = new Security();
        if (!scr.isUserAuthenticated(authString)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        JSONObject request = new JSONObject(cadena);
        String accountId = "";
        JsonObject jsonb = new JsonObject();
        List<AccountHoldersDTO> listaHolders = null;
        try {
            accountId = request.getString("accountId");
        } catch (Exception e) {
            System.out.println("Error:" + e.getMessage());
        }
        System.out.println("accountId:" + accountId);
        TransfersDAO dao = new TransfersDAO();
        String msj = "";
        try {
            
            listaHolders = dao.accountHolders(accountId);
            jsonb.put("holders", listaHolders);
            return Response.status(Response.Status.OK).entity(jsonb).build();
        } catch (Exception e) {
            dao.cerrar();
            System.out.println("Error en response:" + e.getMessage());
        } finally {
            dao.cerrar();
        }
        return null;
    }

    @POST
    @Path("/internal/validate")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response validateInternalAccount(String cadena, @HeaderParam("authorization") String authString) {
         Security scr = new Security();
        
        System.out.println("Request cadenaaaaaaaaaaaaaaaa internal validate:"+cadena);
        if (!scr.isUserAuthenticated(authString)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        AccountsDAO acDao = new AccountsDAO();
        String accountId = "";
        System.out.println("Cadena:" + cadena);
        try {
            JSONObject jsonRecibido = new JSONObject(cadena);
            System.out.println("JsonRecibido:" + jsonRecibido);
            accountId = jsonRecibido.getString("accountNumber");
            if(accountId.equals("053472372372")){
                
            }else{
            int p = Integer.parseInt(accountId.substring(6, 11));
            List<AccountHoldersDTO> listaHolder = acDao.validateInternalAccount(accountId);
            AccountHoldersDTO holder = listaHolder.get(0);
            javax.json.JsonObject create = null;
            create = Json.createObjectBuilder().add("accountId", accountId).add("accountType", acDao.accountType(p).toUpperCase()).add("holders", Json.createArrayBuilder().add((JsonValue) Json.createObjectBuilder().add("customerId","01010110021543").add("name", holder.getName()).add("relationCode", holder.getRelationCode()).build())).add("displayAccountNumber","*******510").build();
            return Response.status(Response.Status.OK).entity(create).build();
             
            }
            
        
         
        } catch (Exception e) {
            System.out.println("Error al obtener objetos Json:" + e.getMessage());
        } finally {
            acDao.cerrar();
        }
        return null;
    }

    @POST
    @Path("/statements")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response statements(String cadena, @HeaderParam("authorization") String authString) {
          
        Security scr = new Security();
        System.out.println("cadena:"+cadena);
        if (!scr.isUserAuthenticated(authString)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        AccountsDAO acDao = new AccountsDAO();
        String accountId = "";
        int pageSize = 0;
        int pageStartIndex = 0;
        try {
            JSONObject jsonRecibido = new JSONObject(cadena);
            JSONArray listaFil = jsonRecibido.getJSONArray("filters");
            System.out.println("ListaFil:" + listaFil);
            System.out.println("Cadenaaaaa:"+cadena);
            String id = "";
            String fd = "";
            for (int i = 0; i < listaFil.length(); i++) {
                JSONObject js = (JSONObject) listaFil.get(0);
                JSONObject js1 = (JSONObject) listaFil.get(1);
                id = js.getString("value");
                fd = js1.getString("value");
                System.out.println("id:" + id + ",fd:" + fd);
            }
            accountId = jsonRecibido.getString("accountId");
            System.out.println("AccountId:" + accountId);
            String nombrePDF = acDao.statements(accountId, id, fd, pageStartIndex, pageSize);

            JsonObject create = null;
            JsonArrayBuilder listaJson = Json.createArrayBuilder();

            javax.json.JsonObject jsi = Json.createObjectBuilder().add("statementId", accountId).add("dateFrom", id).add("dateTo", fd).add("displayName", accountId).add("availableFormats", Json.createArrayBuilder().add((JsonValue) Json.createObjectBuilder().add("type", "PDF").add("fileId", nombrePDF.replace(".pdf", "")).build())).build();
            listaJson.add((JsonValue) jsi);

            javax.json.JsonObject Found = Json.createObjectBuilder().add("statements", listaJson).build();
            return Response.status(Response.Status.OK).entity(Found).build();
        } catch (Exception e) {
            System.out.println("Error al obtener objetos Json:" + e.getMessage());
        } finally {
            acDao.cerrar();
        }
        return null;
    }

    @POST
    @Path("/holds")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response Holds(String cadena, @HeaderParam("authorization") String authString) {
         Security scr = new Security();
        if (!scr.isUserAuthenticated(authString)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        JSONObject jsonre = new JSONObject(cadena);
        String accountId = "";
        AccountsDAO dao = new AccountsDAO();

        try {
            accountId = jsonre.getString("accountId");
            List<HoldsDTO> lista = dao.holds(accountId);
            JsonObject create = null;
            JsonArrayBuilder listaJson = Json.createArrayBuilder();
            for (int i = 0; i < lista.size(); i++) {
                HoldsDTO dto = lista.get(i);
                String ff = String.valueOf(dto.getEntryDate()) + " 00:00:00";
                Timestamp tss = Timestamp.valueOf(ff);
                System.out.println("tss:" + tss);
                ZonedDateTime zonedDateTime = ZonedDateTime.parse(dto.getEntryDate()+"T00:00:00.000-07:00");
                String feR = String.valueOf(zonedDateTime);
                System.out.println("feR:" + feR);
                javax.json.JsonObject jsi = Json.createObjectBuilder().add("holdId", dto.getHoldId()).add("amount", Json.createObjectBuilder().add("amount", dto.getAmount().doubleValue()).add("currencyCode", "MXN").build()).add("entryDate", feR).add("description", dto.getDescritpion()).build();
                listaJson.add((JsonValue) jsi);
            }
            javax.json.JsonObject Found = Json.createObjectBuilder().add("holds", listaJson).build();
            return Response.status(Response.Status.OK).entity(Found).build();
        } catch (Exception e) {
            System.out.println("Error:" + e.getMessage());
            return null;
        }
    }

    @POST
    @Path("/history")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response History(String cadena, @HeaderParam("authorization") String authString) {
         Security scr = new Security();
        if (!scr.isUserAuthenticated(authString)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        String accountId = "", initialDate = "", finalDate = "";
        int pageSize = 0, pageStartIndex = 0;
        AccountsDAO dao = new AccountsDAO();
        JSONObject jsonRecibido = new JSONObject(cadena);
        try {
            JSONArray listaFil = jsonRecibido.getJSONArray("filters");
            System.out.println("ListaFil:" + listaFil);
            initialDate = "";
            finalDate = "";
            accountId = jsonRecibido.getString("accountId");
            pageSize = jsonRecibido.getInt("pageSize");
            pageStartIndex = jsonRecibido.getInt("page");
            for (int i = 0; i < listaFil.length(); i++) {
                JSONObject js = (JSONObject) listaFil.get(0);
                JSONObject js1 = (JSONObject) listaFil.get(1);
                initialDate = js.getString("value");
                finalDate = js1.getString("value");
                System.out.println("id:" + initialDate + ",fd:" + finalDate);
            }
            List<transferencias_completadas_siscoop> lista = dao.History(accountId, initialDate, finalDate, pageSize, pageStartIndex);
            JsonObject create = null;
            JsonArrayBuilder listaJson = Json.createArrayBuilder();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
            String fe = "";
            for (int j = 0; j < lista.size(); j++) {
                transferencias_completadas_siscoop dto = lista.get(j);
                System.out.println("dto:"+dto);
                fe = sdf.format(dto.getFechaejecucion()).replace("/","-");
                ZonedDateTime zonedDateTime = ZonedDateTime.parse(fe+"T00:00:00.000-07:00");
                String feR = String.valueOf(zonedDateTime);
                System.out.println("DTOCtaOrigen:"+dto.getCuentaorigen());
                int o=Integer.parseInt(dto.getCuentaorigen().substring(0,6));
                int p=Integer.parseInt(dto.getCuentaorigen().substring(6,11));
                int a=Integer.parseInt(dto.getCuentaorigen().substring(11,19));
                
                int o1=Integer.parseInt(accountId.substring(0,6));
                int p1=Integer.parseInt(accountId.substring(6,11));
                int a1=Integer.parseInt(accountId.substring(11,19));
                int co=o+p+a;
                int co1=o1+p1+a1;             
                if(co==co1){
                    dto.setMonto(-dto.getMonto());
                }else{
                    dto.setMonto(dto.getMonto());
                }
                javax.json.JsonObject jsi = Json.createObjectBuilder().add("transactionId", String.valueOf(dto.getId())).add("amount",Json.createObjectBuilder().add("amount", dto.getMonto()).add("currencyCode", "MXN").build()).add("postingDate",feR).add("valueDate", fe.replace("/","-")).add("runningBalance",Json.createObjectBuilder().add("amount", dto.getRunningBalance()).add("currencyCode", "MXN").build()).add("description", dto.getComentario1()).add("originatorReferenceId", String.valueOf(dto.getId())).add("originatorCode", String.valueOf(dto.getId())).add("description2",Json.createObjectBuilder().add("value",String.valueOf(dto.getId())).add("valueType", "string").add("isSensitive",false).build()).build();
                listaJson.add((JsonValue) jsi);
            }
            javax.json.JsonObject Found = Json.createObjectBuilder().add("totalRecords", lista.size()).add("queryId", "").add("transactions", listaJson).build();
            return Response.status(Response.Status.OK).entity(Found).build();
        } catch (Exception e) {
            System.out.println("Error:" + e.getMessage());
        } finally {
            dao.cerrar();
        }
        return null;
    }

    @POST
    @Path("/details")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response Details(String cadena, @HeaderParam("authorization") String authString) {
        Security scr = new Security();
        if (!scr.isUserAuthenticated(authString)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        String accountId = "";
        AccountsDAO dao = new AccountsDAO();
        JSONObject jsonRecibido = new JSONObject(cadena);
        try {
            accountId = jsonRecibido.getString("accountId");
            DetailsAccountDTO dto = dao.detailsAccount(accountId);
            System.out.println("DTO:" + dto);
            JsonObject create = null;
            javax.json.JsonObject jsi = Json.createObjectBuilder().add("accountDetails", Json.createObjectBuilder()
                    .add("accountId", dto.getAccountId())
                    .add("accountNumber", dto.getAccountNumber())
                    .add("displayAccountNumber", dto.getDisplayAccountNumber())
                    .add("accountType", dto.getAccountType())
                    .add("currencyCode", dto.getCurrencyCode())
                    .add("productCode", dto.getProductCode())
                    .add("status", dto.getStatus())
                    .add("branch", (JsonValue) Json.createObjectBuilder()
                            .add("value", dto.getSucursal())
                            .add("valueType", "string")
                            .add("isSensitive", false)
                            .build())
                    .add("openedDate", (JsonValue) Json.createObjectBuilder()
                            .add("value", dto.getOpenedDate())
                            .add("valueType", "date")
                            .add("isSensitive", false)
                            .build())
                    .add("iban", (JsonValue) Json.createObjectBuilder()
                            .add("description", "IBAN")
                            .add("isSensitive", true)
                            .add("valueType", "string")
                            .add("value", "******" + dto.getAccountId().substring(15, 19))
                            .build())
                    .build()).build();
            return Response.status(Response.Status.OK).entity(jsi).build();
        } catch (Exception e) {
            System.out.println("Error:" + e.getMessage());
        } finally {
            dao.cerrar();
        }
        return null;
    }

    @POST
    @Path("/create/validate")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response validateCreate(String cadena) {
        try {
            org.codehaus.jettison.json.JSONObject jsonR = new org.codehaus.jettison.json.JSONObject(cadena);
            String accountType = "", customerId = "", productType = "";
            int page = 0, pageSize = 0;
            accountType = jsonR.getString("accountType");
            customerId = jsonR.getString("customerId");
            productType = jsonR.getString("productCode");

            javax.json.JsonObject json = Json.createObjectBuilder().add("validationId", "LK890007")
                    .add("fees", Json.createArrayBuilder())
                    .add("executionDate", "2021-05-12")
                    .build();

            return Response.status(Response.Status.OK).entity(json).build();

        } catch (JSONException ex) {
            System.out.println("Error al crear json:" + ex.getMessage());
        }
        return null;

    }

    @POST
    @Path("/create/execute")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response executeCreate(String cadena) {
        try {
            org.codehaus.jettison.json.JSONObject jsonR = new org.codehaus.jettison.json.JSONObject(cadena);
            String validationId = "";

            validationId = jsonR.getString("validationId");

            javax.json.JsonObject json = Json.createObjectBuilder().add("status", "completed")
                    .add("accountId", "TIME")
                    .add("accountNumber", "67564236465646")
                    .build();

            return Response.status(Response.Status.OK).entity(json).build();

        } catch (JSONException ex) {
            System.out.println("Error al crear json:" + ex.getMessage());
        }
        return null;

    }

    @POST
    @Path("/status/change/validate")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response changeStatus(String cadena, @HeaderParam("authorization") String authString) {
         Security scr = new Security();
        if (!scr.isUserAuthenticated(authString)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        JSONObject jsonRecibido = new JSONObject(cadena);
        javax.json.JsonObject json = null;
        try {
            json = Json.createObjectBuilder().add("validationId", "0967998686787").add("fees", Json.createArrayBuilder()).add("effectiveDate", "").build();
        } catch (Exception e) {
            System.out.println("Error:" + e.getMessage());
        }
        return Response.status(Response.Status.OK).entity(json).build();
    }

    @POST
    @Path("/status/change/execute")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response changeStatusExecute(String cadena, @HeaderParam("authorization") String authString) {
         Security scr = new Security();
        if (!scr.isUserAuthenticated(authString)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        JSONObject jsonRecibido = new JSONObject(cadena);
        javax.json.JsonObject json = null;
        try {
            json = Json.createObjectBuilder().add("status", "completed").build();
        } catch (Exception e) {
            System.out.println("Error:" + e.getMessage());
        }
        return Response.status(Response.Status.OK).entity(json).build();
    }

   
}
