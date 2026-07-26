package org.example.storemanager.repository.omnichannel;

import org.example.storemanager.entity.omnichannel.SalesChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface SalesChannelRepository extends JpaRepository<SalesChannel, Long> {
    Optional<SalesChannel> findByIdAndIsDeletedFalse(Long id);
    List<SalesChannel> findByIsDeletedFalse();
}
