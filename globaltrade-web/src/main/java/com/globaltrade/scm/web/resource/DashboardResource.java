package com.globaltrade.scm.web.resource;

import com.globaltrade.scm.api.*;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*; import jakarta.ws.rs.core.MediaType; import java.util.*;
@Path("/dashboard") @Produces(MediaType.APPLICATION_JSON)
public class DashboardResource { @EJB private OperationsFacadeLocal facade; @GET public Map<String,Object> get() throws BusinessFault { return facade.dashboard(); } }
