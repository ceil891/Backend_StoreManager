package org.example.storemanager.service.system.impl;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.config.LogActivity;
import org.example.storemanager.dto.request.system.posSession.CreatePosSessionRequest;
import org.example.storemanager.dto.response.system.posSession.PosSessionResponse;
import org.example.storemanager.entity.system.Branch;
import org.example.storemanager.entity.system.PosSession;
import org.example.storemanager.entity.system.User;
import org.example.storemanager.enums.system.PosSessionStatus;
import org.example.storemanager.exception.ResourceNotFoundException;
import org.example.storemanager.repository.system.BranchRepository;
import org.example.storemanager.repository.system.PosSessionRepository;
import org.example.storemanager.repository.system.UserRepository;
import org.example.storemanager.service.system.PosSessionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PosSessionServiceImpl implements PosSessionService {

    private final PosSessionRepository repository;
    private final UserRepository userRepository;
    private final BranchRepository branchRepository;

    @Override
    @LogActivity(actionType = "CREATE", entityName = "PosSession", entityClass = PosSession.class)
    public PosSessionResponse startSession(CreatePosSessionRequest request) {
        // Sửa lại Exception theo đúng cấu trúc dự án của bạn
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));

        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", request.getBranchId()));

        PosSession session = PosSession.builder()
                .startTime(LocalDateTime.now())
                .openingCash(request.getOpeningCash())
                .status(PosSessionStatus.OPEN)
                .user(user)
                .branch(branch)
                .build();

        return mapToResponse(repository.save(session));
    }

    @Override
    @LogActivity(actionType = "UPDATE", entityName = "PosSession", entityClass = PosSession.class)
    public PosSessionResponse endSession(Long id, BigDecimal actualClosingCash) {
        PosSession session = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PosSession", "id", id));

        session.setEndTime(LocalDateTime.now());
        session.setActualClosingCash(actualClosingCash);
        session.setStatus(PosSessionStatus.CLOSED);

        return mapToResponse(repository.save(session));
    }

    @Override
    public Page<PosSessionResponse> getAllSessions(Pageable pageable) {
        return repository.findAll(pageable).map(this::mapToResponse);
    }

    @Override
    public PosSessionResponse getSessionById(Long id) {
        PosSession session = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PosSession", "id", id));
        return mapToResponse(session);
    }

    private PosSessionResponse mapToResponse(PosSession session) {
        return PosSessionResponse.builder()
                .id(session.getId())
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .openingCash(session.getOpeningCash())
                .actualClosingCash(session.getActualClosingCash())
                .status(session.getStatus())
                .username(session.getUser() != null ? session.getUser().getUsername() : null)
                // Lưu ý: Kiểm tra lại phương thức trong Branch entity (getName() hay getBranchName())
                .branchName(session.getBranch() != null ? session.getBranch().getBranchName() : null)
                .build();
    }
}