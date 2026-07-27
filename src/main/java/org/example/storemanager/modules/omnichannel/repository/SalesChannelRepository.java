package org.example.storemanager.modules.omnichannel.repository;

import org.example.storemanager.modules.omnichannel.entity.SalesChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface SalesChannelRepository extends JpaRepository<SalesChannel, Long> {
    Optional<SalesChannel> findByIdAndIsDeletedFalse(Long id);
    List<SalesChannel> findByIsDeletedFalse();
}
