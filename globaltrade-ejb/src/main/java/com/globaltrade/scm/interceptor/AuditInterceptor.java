package com.globaltrade.scm.interceptor;

import com.globaltrade.scm.entity.*;
import com.globaltrade.scm.service.AuditService;
import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.SessionContext;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;

public class AuditInterceptor {
    @EJB private AuditService auditService;
    @Resource private SessionContext sessionContext;

    @AroundInvoke
    public Object audit(InvocationContext context) throws Exception {
        String username = actorIdentity();

        String component = context.getTarget().getClass().getSimpleName();
        String operation = context.getMethod().getName();
        try {
            Object result = context.proceed();
            auditService.record(username, component, operation, true, successDetail(operation, context.getParameters(), result));
            return result;
        } catch (Exception ex) {
            String detail = ex.getMessage();
            if (detail == null || detail.isBlank()) detail = ex.getClass().getSimpleName();
            auditService.record(username, component, operation, false, detail);
            throw ex;
        }
    }

    private String successDetail(String operation, Object[] args, Object result) {
        if (result instanceof Vendor vendor) {
            if ("create".equals(operation)) return "Created partner " + vendor.getVendorCode() + " - " + vendor.getName();
            if ("deactivate".equals(operation)) return "Deactivated partner " + vendor.getVendorCode();
            if ("reactivate".equals(operation)) return "Reactivated partner " + vendor.getVendorCode();
            if ("reviewSupply".equals(operation)) {
                String rating = args.length > 1 ? String.valueOf(args[1]) : "reviewed";
                String inventoryRef = args.length > 0 ? String.valueOf(args[0]) : "unknown";
                String note = args.length > 2 && args[2] != null && !String.valueOf(args[2]).isBlank()
                        ? "; note: " + String.valueOf(args[2]).trim() : "";
                return "Reviewed partner " + vendor.getVendorCode() + " at " + rating
                        + "/5 for received inventory #" + inventoryRef + note;
            }
        }
        if (result instanceof InventoryItem item) {
            if ("create".equals(operation)) {
                String warehouse = item.getWarehouse() == null ? "unknown warehouse" : item.getWarehouse().getName();
                int received = args.length > 2 ? Integer.parseInt(String.valueOf(args[2])) : 0;
                return "Received " + received + " units of " + item.getItemName()
                        + " into GlobalTrade inventory as " + item.getSku() + " at " + warehouse;
            }
            if ("adjust".equals(operation) && args.length > 1) {
                int delta = Integer.parseInt(String.valueOf(args[1]));
                String action = delta >= 0 ? "Added " + delta + " units to " : "Removed " + Math.abs(delta) + " units from ";
                return action + item.getSku() + "; available stock is now " + item.getQuantity() + " units";
            }
        }
        if (result instanceof Shipment shipment) {
            if ("create".equals(operation)) {
                int totalQuantity = shipment.getItems() == null ? 0 : shipment.getItems().stream().mapToInt(ShipmentItem::getQuantity).sum();
                InventoryItem stock = shipment.getItems() == null || shipment.getItems().isEmpty() ? null : shipment.getItems().get(0).getInventoryItem();
                String product = stock == null ? "cargo" : stock.getItemName();
                int records = shipment.getItems() == null ? 0 : shipment.getItems().size();
                return "Created shipment " + shipment.getTrackingNumber() + " for " + totalQuantity + " units of " + product
                        + " from GlobalTrade inventory using " + records + " stock record" + (records == 1 ? "" : "s")
                        + "; route " + shipment.getOrigin() + " to " + shipment.getDestination() + " using " + shipment.getCarrier();
            }
            if ("updateStatus".equals(operation) && args.length > 1) {
                String status = String.valueOf(args[1]);
                return switch (status) {
                    case "READY" -> "Marked shipment " + shipment.getTrackingNumber() + " ready for clearance and dispatch";
                    case "IN_TRANSIT" -> "Dispatched shipment " + shipment.getTrackingNumber() + "; it is now in transit";
                    case "DELIVERED" -> "Completed delivery for shipment " + shipment.getTrackingNumber();
                    case "CANCELLED" -> "Cancelled shipment " + shipment.getTrackingNumber() + " and returned reserved stock";
                    default -> "Updated shipment " + shipment.getTrackingNumber() + " to " + status;
                };
            }
            if ("cancel".equals(operation)) return "Cancelled shipment " + shipment.getTrackingNumber() + " and returned reserved stock";
        }
        if (result instanceof CustomsDocument doc && "updateStatus".equals(operation)) {
            return switch (doc.getStatus()) {
                case SUBMITTED -> "Submitted clearance " + doc.getReferenceNo() + " for review";
                case APPROVED -> "Approved clearance " + doc.getReferenceNo() + "; the shipment may be dispatched";
                case REJECTED -> "Rejected clearance " + doc.getReferenceNo() + "; shipment cancelled and reserved stock returned";
                case PENDING -> "Clearance " + doc.getReferenceNo() + " is pending";
                case CANCELLED -> "Cancelled clearance " + doc.getReferenceNo();
            };
        }
        return "Operation completed successfully";
    }
    private String actorIdentity() {
        try {
            if (sessionContext == null || sessionContext.getCallerPrincipal() == null) return "SYSTEM";
            String username = sessionContext.getCallerPrincipal().getName();
            if (username == null || username.isBlank() || "anonymous".equalsIgnoreCase(username)) return "SYSTEM";
            return username.trim();
        } catch (Exception ignored) {
            return "SYSTEM";
        }
    }
}
