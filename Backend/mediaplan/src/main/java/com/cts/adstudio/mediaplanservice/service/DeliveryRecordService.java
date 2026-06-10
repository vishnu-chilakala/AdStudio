package com.cts.adstudio.mediaplanservice.service;

import com.cts.adstudio.mediaplanservice.dto.request.DeliveryRecordRequest;
import com.cts.adstudio.mediaplanservice.dto.response.DeliveryRecordResponse;
import java.util.List;

public interface DeliveryRecordService {
    DeliveryRecordResponse recordDelivery(DeliveryRecordRequest request);
    List<DeliveryRecordResponse> getDeliveryByLineItem(Integer lineItemId);
}