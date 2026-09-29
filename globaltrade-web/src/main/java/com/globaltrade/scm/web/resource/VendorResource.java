package com.globaltrade.scm.web.resource;

import com.globaltrade.scm.api.BusinessFault;
import com.globaltrade.scm.api.OperationsFacadeLocal;
import jakarta.ejb.EJB;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.util.List;
import java.util.Map;

@Path("/vendors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VendorResource {

    @EJB
    private OperationsFacadeLocal facade;

    @GET
    public List<Map<String, Object>> all() throws BusinessFault {
        return facade.vendors();
    }

    @POST
    public Response create(Map<String, Object> body, @Context UriInfo uri) throws BusinessFault {
        Map<String, Object> created = facade.createVendor(body);
        return Response.created(uri.getAbsolutePathBuilder()
                        .path(String.valueOf(created.get("id")))
                        .build())
                .entity(created)
                .build();
    }

    @GET
    @Path("/{id}/reviews")
    public List<Map<String, Object>> reviews(@PathParam("id") long id) throws BusinessFault {
        return facade.vendorReviews(id);
    }

    @DELETE
    @Path("/{id}")
    public Map<String, Object> deactivate(@PathParam("id") long id) throws BusinessFault {
        return facade.deactivateVendor(id);
    }

    @POST
    @Path("/{id}/reactivate")
    public Map<String, Object> reactivate(@PathParam("id") long id) throws BusinessFault {
        return facade.reactivateVendor(id);
    }
}
