/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.config;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import it.csi.accrual.accrualsbsrv.dto.Base;

// Mixin class for Base to customize id deserialization
abstract class BaseMixin {
    @JsonDeserialize(using = JsonConfig.FlexibleLongDeserializer.class)
    abstract Long getId();

    void setId(Long id) {}
}