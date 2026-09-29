package com.globaltrade.scm.web.resource;

import com.globaltrade.scm.api.*;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*; import jakarta.ws.rs.core.*; import java.util.*;
@Path("/shipments") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
public class ShipmentResource {
 @EJB private OperationsFacadeLocal facade;
 @GET public List<Map<String,Object>> all() throws BusinessFault {return facade.shipments();}
 @GET @Path("/{id}") public Map<String,Object> one(@PathParam("id") long id) throws BusinessFault {return facade.shipment(id);}
 @POST public Response create(Map<String,Object> body,@Context UriInfo uri) throws BusinessFault {Map<String,Object> m=facade.createShipment(body);return Response.created(uri.getAbsolutePathBuilder().path(String.valueOf(m.get("id"))).build()).entity(m).build();}
 @PUT @Path("/{id}/status") public Map<String,Object> status(@PathParam("id")long id,Map<String,Object>b)throws BusinessFault{return facade.updateShipmentStatus(id,String.valueOf(b.get("status")));}
 @POST @Path("/{id}/cancel") public Map<String,Object> cancel(@PathParam("id")long id)throws BusinessFault{return facade.cancelShipment(id);}
}
