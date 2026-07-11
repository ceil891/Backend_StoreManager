package org.example.storemanager.repository.media;

import org.example.storemanager.entity.media.MediaAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MediaAssetRepository extends JpaRepository<MediaAsset, Long> {
}
