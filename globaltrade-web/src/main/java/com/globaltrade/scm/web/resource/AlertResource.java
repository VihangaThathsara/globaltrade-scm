package com.globaltrade.scm.web.resource;

import com.globaltrade.scm.api.*;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*; import jakarta.ws.rs.core.MediaType; import java.util.*;
@Path("/alerts") @Produces(MediaType.APPLICATION_JSON)
public class AlertResource { @EJB private OperationsFacadeLocal facade; @GET public List<Map<String,Object>> all()throws BusinessFault{return facade.alerts();} @POST @Path("/{id}/resolve") public Map<String,Object> resolve(@PathParam("id")long id)throws BusinessFault{facade.resolveAlert(id);return Map.of("resolved",true);} }
