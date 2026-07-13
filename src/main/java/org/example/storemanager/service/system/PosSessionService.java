package org.example.storemanager.service.system;

import org.example.storemanager.dto.request.system.posSession.CreatePosSessionRequest;
import org.example.storemanager.dto.response.system.posSession.PosSessionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;

public interface PosSessionService {
    PosSessionResponse startSession(CreatePosSessionRequest request);
    PosSessionResponse endSession(Long id, BigDecimal actualClosingCash);
    Page<PosSessionResponse> getAllSessions(Pageable pageable);
    PosSessionResponse getSessionById(Long id);
}