package org.example.storemanager.repository.omnichannel;

import org.example.storemanager.entity.omnichannel.ChannelProductMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface ChannelProductMappingRepository extends JpaRepository<ChannelProductMapping, Long> {
    Optional<ChannelProductMapping> findByIdAndIsDeletedFalse(Long id);
    List<ChannelProductMapping> findByIsDeletedFalse();
}
