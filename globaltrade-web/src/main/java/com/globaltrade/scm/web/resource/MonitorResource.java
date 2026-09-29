package com.globaltrade.scm.web.resource;

import com.globaltrade.scm.api.*;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*; import jakarta.ws.rs.core.MediaType; import java.util.*;
@Path("/monitor") @Produces(MediaType.APPLICATION_JSON)
public class MonitorResource { @EJB private OperationsFacadeLocal facade; @GET public Map<String,Object> get()throws BusinessFault{return facade.monitor();} }
