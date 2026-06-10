package com.cts.adstudio.mediaplanservice.service;

import com.cts.adstudio.mediaplanservice.dto.request.InsertionOrderRequest;
import com.cts.adstudio.mediaplanservice.dto.response.InsertionOrderResponse;
import java.util.List;

public interface InsertionOrderService {
    InsertionOrderResponse createInsertionOrder(InsertionOrderRequest request);
    InsertionOrderResponse getInsertionOrderById(Integer ioId);
    List<InsertionOrderResponse> getAllInsertionOrders();
    List<InsertionOrderResponse> getInsertionOrdersByPublisher(Integer publisherId);
    InsertionOrderResponse updateStatus(Integer ioId, String status);
}