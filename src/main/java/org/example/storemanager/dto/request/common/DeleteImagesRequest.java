package org.example.storemanager.dto.request.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteImagesRequest {
    private List<String> publicIds;
}
