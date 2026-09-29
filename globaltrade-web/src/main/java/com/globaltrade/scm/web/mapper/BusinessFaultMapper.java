package com.globaltrade.scm.web.mapper;

import com.globaltrade.scm.api.BusinessFault;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.LinkedHashMap;
import java.util.Map;

@Provider
public class BusinessFaultMapper implements ExceptionMapper<BusinessFault> {
    @Override
    public Response toResponse(BusinessFault fault) {
        Map<String,Object> body = new LinkedHashMap<>();
        body.put("error", fault.getMessage());
        body.put("code", fault.getCode());
        return Response.status(fault.getStatus()).type(MediaType.APPLICATION_JSON).entity(body).build();
    }
}
