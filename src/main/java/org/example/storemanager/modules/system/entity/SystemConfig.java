package org.example.storemanager.modules.system.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;

@Entity
@Table(name = "system_configs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class SystemConfig extends BaseEntity {

    @Column(name = "config_key", nullable = false, unique = true, length = 100)
    @JsonAlias({"configKey", "key"})
    private String configKey;

    @Column(name = "config_value", columnDefinition = "TEXT")
    @JsonAlias({"configValue", "value"})
    private String configValue;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 50)
    private String category;

    @Column(name = "data_type", length = 30)
    private String dataType;

    @Column(name = "is_encrypted")
    private Boolean isEncrypted;

    @Column(name = "requires_reboot")
    private Boolean requiresRebootToApply;

    @Column(name = "updated_by_role", length = 50)
    private String updatedByRole;

    public String getValue() {
        return configValue;
    }
}