/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

// PrimeNG Components/Modules
import { TabView } from 'primeng/tabview';
import { DropdownModule } from 'primeng/dropdown'; // back to Module
import { InputText } from 'primeng/inputtext';
import { Button } from 'primeng/button';
import { Calendar } from 'primeng/calendar';
import { InputTextarea } from 'primeng/inputtextarea';
import { Checkbox } from 'primeng/checkbox';
import { TableModule } from 'primeng/table'; // back to Module
import { DynamicDialogModule } from 'primeng/dynamicdialog';
import { Toast } from 'primeng/toast';
import { MessagesModule } from 'primeng/messages'; // back to Module
import { Message } from 'primeng/message';

@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    TabView,
    DropdownModule, // back to Module
    InputText,
    Button,
    Calendar,
    InputTextarea,
    Checkbox,
    TableModule, // back to Module
    DynamicDialogModule,
    Toast,
    MessagesModule, // back to Module
    Message,
  ],
  exports: [
    TabView,
    DropdownModule, // back to Module
    InputText,
    Button,
    Calendar,
    InputTextarea,
    Checkbox,
    TableModule, // back to Module
    DynamicDialogModule,
    Toast,
    MessagesModule, // back to Module
    Message,
  ]
})
export class PrimeNgModule { }
