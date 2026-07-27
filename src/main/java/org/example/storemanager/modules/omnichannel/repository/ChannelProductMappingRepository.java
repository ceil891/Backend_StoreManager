package org.example.storemanager.modules.omnichannel.repository;

import org.example.storemanager.modules.omnichannel.entity.ChannelProductMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface ChannelProductMappingRepository extends JpaRepository<ChannelProductMapping, Long> {
    Optional<ChannelProductMapping> findByIdAndIsDeletedFalse(Long id);
    List<ChannelProductMapping> findByIsDeletedFalse();
}
