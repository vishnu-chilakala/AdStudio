package com.cts.adstudio.mediaplanservice.service;

import com.cts.adstudio.mediaplanservice.dto.response.PacingAlertResponse;
import java.util.List;

public interface PacingAlertService {
    int runPacingCheck();                                  // the engine — returns # alerts created
    List<PacingAlertResponse> getAlertsByStatus(String status);
    PacingAlertResponse updateAlertStatus(Integer alertId, String status);
}