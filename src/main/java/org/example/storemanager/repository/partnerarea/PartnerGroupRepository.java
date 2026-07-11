package org.example.storemanager.repository.partnerarea;

import org.example.storemanager.entity.partnerarea.PartnerGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PartnerGroupRepository extends JpaRepository<PartnerGroup, Long>, JpaSpecificationExecutor<PartnerGroup> {
    boolean existsByGroupCode(String groupCode);
    Page<PartnerGroup> findByGroupNameContainingIgnoreCaseOrType(String groupName, String type, Pageable pageable);
}