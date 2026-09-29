package com.globaltrade.scm.web.resource;

import com.globaltrade.scm.api.*;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*; import jakarta.ws.rs.core.MediaType; import java.util.*;
@Path("/customs") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
public class CustomsResource {
 @EJB private OperationsFacadeLocal facade;
 @GET public List<Map<String,Object>> all()throws BusinessFault{return facade.customs();}
 @PUT @Path("/{id}/status") public Map<String,Object> status(@PathParam("id")long id,Map<String,Object>b)throws BusinessFault{return facade.updateCustomsStatus(id,String.valueOf(b.get("status")));}
 @POST @Path("/review") public Map<String,Object> review()throws BusinessFault{return Map.of("overdue",facade.reviewCustomsDeadlines());}
}
