package com.shopease.gateway.store;

import com.shopease.gateway.dto.GatewayResponse;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileStore {

    private static final String FILE_PATH = "payments.json";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static synchronized void save(GatewayResponse gatewayResponse) {
        try {
            File file = new File(FILE_PATH);
            List<GatewayResponse> existing = new ArrayList<>();

            // Read existing data
            if (file.exists()) {
                existing = objectMapper.readValue(file,
                        objectMapper
                                .getTypeFactory()
                                .constructCollectionType(List.class, GatewayResponse.class));
            }
            // Add new record
            existing.add(gatewayResponse);

            // write back
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(file, existing);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to save gateway response in File: ", ex);
        }
    }

}
