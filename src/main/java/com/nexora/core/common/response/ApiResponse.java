package com.nexora.core.common.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiResponse<T> {
    private Boolean success;
    private String message;
    private T data;
}
