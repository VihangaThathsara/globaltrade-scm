package com.globaltrade.scm.web.resource;

import com.globaltrade.scm.api.BusinessFault;
import com.globaltrade.scm.api.OperationsFacadeLocal;
import jakarta.ejb.EJB;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
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

@Path("/inventory")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class InventoryResource {
    @EJB
    private OperationsFacadeLocal facade;

    @GET
    public List<Map<String,Object>> all() throws BusinessFault {
        return facade.inventory();
    }

    @GET
    @Path("/warehouses")
    public List<Map<String,Object>> warehouses() throws BusinessFault {
        return facade.warehouses();
    }

    @POST
    public Response create(Map<String,Object> body, @Context UriInfo uri) throws BusinessFault {
        Map<String,Object> created = facade.createInventory(body);
        return Response.created(uri.getAbsolutePathBuilder().path(String.valueOf(created.get("id"))).build())
                .entity(created)
                .build();
    }

    @POST
    @Path("/{id}/adjust")
    public Map<String,Object> adjust(@PathParam("id") long id, Map<String,Object> body) throws BusinessFault {
        return facade.adjustInventory(id, ((Number) body.get("delta")).intValue());
    }

    @POST
    @Path("/{id}/partner-review")
    public Map<String,Object> reviewPartner(@PathParam("id") long inventoryItemId, Map<String,Object> body) throws BusinessFault {
        Object ratingValue = body == null ? null : body.get("rating");
        if (ratingValue == null || String.valueOf(ratingValue).isBlank()) {
            throw new BadRequestException("Supplier rating is required");
        }
        double rating = ratingValue instanceof Number number ? number.doubleValue() : Double.parseDouble(String.valueOf(ratingValue));
        String notes = String.valueOf(body.getOrDefault("notes", ""));
        return facade.reviewInventorySupply(inventoryItemId, rating, notes);
    }
}
