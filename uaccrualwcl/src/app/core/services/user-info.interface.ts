/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

export interface UserInfo {
  username: string;
  displayName: string;
  email: string;
  enti: string[];
  permissions: string[];
}