package com.globaltrade.scm.web.resource;

import com.globaltrade.scm.api.*;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*; import jakarta.ws.rs.core.MediaType; import java.util.*;
@Path("/audit") @Produces(MediaType.APPLICATION_JSON)
public class AuditResource { @EJB private OperationsFacadeLocal facade; @GET public List<Map<String,Object>> recent(@QueryParam("limit") @DefaultValue("80") int limit)throws BusinessFault{return facade.audit(limit);} }
