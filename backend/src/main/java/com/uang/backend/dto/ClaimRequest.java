package com.uang.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 认领状态更新请求体。
 * 用于 POST /api/v1/lost-items/{id}/claim 与 POST /api/v1/find-items/{id}/claim。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClaimRequest {

    /** 目标认领状态：true=已认领，false=未认领 */
    private boolean claimed;
}