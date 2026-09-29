package com.globaltrade.scm.api;

import jakarta.ejb.Local;
import java.util.List;
import java.util.Map;

@Local
public interface OperationsFacadeLocal {
    Map<String,Object> dashboard() throws BusinessFault;
    List<Map<String,Object>> shipments() throws BusinessFault;
    Map<String,Object> shipment(long id) throws BusinessFault;
    Map<String,Object> createShipment(Map<String,Object> body) throws BusinessFault;
    Map<String,Object> updateShipmentStatus(long id, String status) throws BusinessFault;
    Map<String,Object> cancelShipment(long id) throws BusinessFault;

    List<Map<String,Object>> inventory() throws BusinessFault;
    List<Map<String,Object>> warehouses() throws BusinessFault;
    Map<String,Object> createInventory(Map<String,Object> body) throws BusinessFault;
    Map<String,Object> adjustInventory(long id, int delta) throws BusinessFault;

    List<Map<String,Object>> vendors() throws BusinessFault;
    Map<String,Object> createVendor(Map<String,Object> body) throws BusinessFault;
    List<Map<String,Object>> vendorReviews(long id) throws BusinessFault;
    Map<String,Object> deactivateVendor(long id) throws BusinessFault;
    Map<String,Object> reactivateVendor(long id) throws BusinessFault;
    Map<String,Object> reviewInventorySupply(long inventoryItemId, double ratingStars, String notes) throws BusinessFault;

    List<Map<String,Object>> customs() throws BusinessFault;
    Map<String,Object> updateCustomsStatus(long id, String status) throws BusinessFault;
    int reviewCustomsDeadlines() throws BusinessFault;

    List<Map<String,Object>> alerts() throws BusinessFault;
    void resolveAlert(long id) throws BusinessFault;
    List<Map<String,Object>> audit(int limit) throws BusinessFault;
    Map<String,Object> monitor() throws BusinessFault;
}
