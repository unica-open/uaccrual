/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators, FormArray, FormControl, ReactiveFormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { ReportService } from '../../services/report.service';
import { ReportInfo, ReportParameter, ParameterOption, RenderSection, GenerateRequest } from '../../models/report.models';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatRadioModule } from '@angular/material/radio';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { TextWidgetComponent } from '../../widgets/standard/text/text-widget.component';
import { NumberWidgetComponent } from '../../widgets/standard/number/number-widget.component';
import { DateWidgetComponent } from '../../widgets/standard/date/date-widget.component';
import { ComboWidgetComponent } from '../../widgets/standard/combo/combo-widget.component';
import { RadioWidgetComponent } from '../../widgets/standard/radio/radio-widget.component';
import { CheckboxWidgetComponent } from '../../widgets/standard/checkbox/checkbox-widget.component';
import { GLOBAL_WIDGET_MAP, TEMPLATE_CONFIGS, findMatch, matchesAny, matchPattern } from '../../config/report-config';

@Component({
  selector: 'app-report-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatCheckboxModule,
    MatRadioModule,
    MatButtonToggleModule,
    MatProgressSpinnerModule,
    TextWidgetComponent,
    NumberWidgetComponent,
    DateWidgetComponent,
    ComboWidgetComponent,
    RadioWidgetComponent,
    CheckboxWidgetComponent
  ],
  templateUrl: './report-form.component.html',
  styleUrls: ['./report-form.component.scss']
})
export class ReportFormComponent implements OnInit, OnDestroy {

  reportForm!: FormGroup;
  reportInfo!: ReportInfo;
  reportNome: string | null = null;
  reportCodice: string | null = null;
  loading = false;
  submitting = false;
  error: string | null = null;
  successMessage: string | null = null;

  selectedFormato = 'pdf';
  formatiDisponibili = [
    { value: 'pdf', label: 'PDF' },
    { value: 'xlsx', label: 'Excel (XLSX)' }
  ];

  private _sections: RenderSection[] = [];
  private routeParamsSub?: Subscription;
  private allOptions = new Map<string, ParameterOption[]>();
  private displayedOptions = new Map<string, ParameterOption[]>();
  private cascadeSubs: Subscription[] = [];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private fb: FormBuilder,
    private reportService: ReportService
  ) { }

  ngOnInit(): void {
    this.routeParamsSub = this.route.queryParams.subscribe(params => {
      this.reportNome = params['nome'] || null;
      this.reportCodice = params['codice'] || null;

      const rptdesign = params['rptdesign'];
      if (rptdesign) {
        this.caricaMetadataReport(rptdesign);
      } else {
        this.error = 'Nessun report specificato';
        this.loading = false;
      }
    });
  }

  ngOnDestroy(): void {
    if (this.routeParamsSub) {
      this.routeParamsSub.unsubscribe();
    }
    this.cascadeSubs.forEach(s => s.unsubscribe());
  }

  caricaMetadataReport(rptdesign: string): void {
    this.loading = true;
    this.error = null;
    this.successMessage = null;

    console.log('Caricamento metadata report:', rptdesign);

    this.reportService.getReportInfo(rptdesign).subscribe({
      next: (info) => {
        console.log('Metadata caricati con successo:', info);
        this.reportInfo = info;

        if (!info.parametri || info.parametri.length === 0) {
          console.warn('Il report non ha parametri definiti');
        }

        this.computeSections();
        this.creaForm(info.parametri);
        this.loading = false;
      },
      error: (err) => {
        console.error('Errore nel caricamento metadata report:', err);

        let errorMessage = 'Impossibile caricare i parametri del report.';

        if (err.status === 400) {
          errorMessage = 'Template non trovato. Verificare che il file esista nella directory dei report.';
        } else if (err.status === 500) {
          errorMessage = 'Errore interno del server. Contattare l\'amministratore di sistema.';
        } else if (err.status === 0) {
          errorMessage = 'Impossibile raggiungere il server. Verificare che il backend sia in esecuzione.';
        }

        this.error = errorMessage;
        this.loading = false;
      }
    });
  }

  creaForm(parametri: ReportParameter[]): void {
    const templateName = this.reportInfo?.template;
    const tplCfg = templateName ? TEMPLATE_CONFIGS[templateName] : undefined;

    const formGroup: { [key: string]: FormControl } = {};
    this.allOptions.clear();
    this.displayedOptions.clear();

    parametri.forEach(param => {
      let valoreIniziale = param.valoreDefault;

      if (tplCfg?.defaultValues) {
        for (const [pattern, fn] of Object.entries(tplCfg.defaultValues)) {
          if (matchPattern(param.nome, pattern as any)) {
            valoreIniziale = fn(this.reportInfo);
            break;
          }
        }
      }

      if (param.tipo === 'DATE' && !valoreIniziale) {
        const oggi = new Date();
        valoreIniziale = oggi.toISOString().split('T')[0];
      }

      const validators = [];
      if (param.obbligatorio && !param.hidden) {
        validators.push(Validators.required);
      }

      formGroup[param.nome] = new FormControl(valoreIniziale, validators);

      if (param.valoriPossibili) {
        this.allOptions.set(param.nome, [...param.valoriPossibili]);
        this.displayedOptions.set(param.nome, [...param.valoriPossibili]);
      }
    });

    this.reportForm = this.fb.group(formGroup);

    this.setupCascadeSubscriptions();
  }

  private filterChildOptions(childName: string, parentValue: any): void {
    const allOpts = this.allOptions.get(childName);
    if (!allOpts) return;

    const filtered = allOpts.filter(o => o.dipendeDa === undefined || o.dipendeDa === null || String(o.dipendeDa) === String(parentValue));

    this.displayedOptions.set(childName, filtered);

    const childControl = this.reportForm?.get(childName);
    if (!childControl) return;

    const childParam = this.reportInfo?.parametri.find(p => p.nome === childName);
    const defaultValue = childParam?.valoreDefault;
    console.log(`[Cascade] child="${childName}" parentValue=${JSON.stringify(parentValue)} defaultValue=${JSON.stringify(defaultValue)} filtered=${filtered.map(o => JSON.stringify(o.valore)).join(', ')} current=${JSON.stringify(childControl.value)}`);
    if (defaultValue !== undefined && filtered.some(o => String(o.valore) === String(defaultValue))) {
      console.log(`[Cascade] Applying defaultValue=${JSON.stringify(defaultValue)}`);
      if (String(childControl.value) !== String(defaultValue)) {
        setTimeout(() => childControl.setValue(defaultValue, { emitEvent: true }));
      }
      return;
    }

    if (filtered.length === 1) {
      const singleOption = filtered[0];
      if (String(childControl.value) !== String(singleOption.valore)) {
        setTimeout(() => childControl.setValue(singleOption.valore, { emitEvent: true }));
      }
    } else if (filtered.length > 1) {
      const currentValue = childControl.value;
      const stillValid = filtered.some(o => String(o.valore) === String(currentValue));
      if (!stillValid) {
        setTimeout(() => childControl.setValue(filtered[0].valore, { emitEvent: true }));
      }
    } else {
      setTimeout(() => childControl.setValue(null, { emitEvent: true }));
    }
  }

  private setupCascadeSubscriptions(): void {
    this.cascadeSubs.forEach(s => s.unsubscribe());
    this.cascadeSubs = [];

    for (const section of this._sections) {
      if (section.type === 'group' && section.parameters && section.parameters.length >= 2) {
        const params = section.parameters;
        for (let i = 1; i < params.length; i++) {
          const child = params[i];
          const parent = params[i - 1];

          const hasDipendeDa = child.valoriPossibili?.some(o => o.dipendeDa != null);
          if (!hasDipendeDa) continue;

          const parentControl = this.reportForm?.get(parent.nome);
          if (!parentControl) continue;

          const childName = child.nome;

          this.filterChildOptions(childName, parentControl.value);

          const sub = parentControl.valueChanges.subscribe(parentValue => {
            this.filterChildOptions(childName, parentValue);
            for (let j = i + 1; j < params.length; j++) {
              const nextParent = params[j - 1];
              const nextChild = params[j];
              const nextControl = this.reportForm?.get(nextParent.nome);
              if (nextControl) {
                this.filterChildOptions(nextChild.nome, nextControl.value);
              }
            }
          });
          this.cascadeSubs.push(sub);
        }
      }
    }
  }

  getOptionsForParam(nome: string): ParameterOption[] {
    return this.displayedOptions.get(nome) ?? [];
  }

  get parametriControls(): { [key: string]: FormControl } {
    return this.reportForm?.controls as { [key: string]: FormControl };
  }

  get sections(): RenderSection[] {
    return this._sections;
  }

  private computeSections(): void {
    if (!this.reportInfo?.parametri) {
      this._sections = [];
      return;
    }

    const sections: RenderSection[] = [];
    let currentGroupName: string | null = null;
    let currentGroupParams: ReportParameter[] = [];
    let ungroupedParams: ReportParameter[] = [];

    const finalizeGroup = () => {
      if (currentGroupName) {
        sections.push({
          type: 'group',
          groupName: currentGroupName,
          parameters: [...currentGroupParams]
        });
        currentGroupName = null;
        currentGroupParams = [];
      }
    };

    const finalizeUngrouped = () => {
      if (ungroupedParams.length > 0) {
        sections.push({ type: 'ungrouped', parameters: [...ungroupedParams] });
        ungroupedParams = [];
      }
    };

    this.reportInfo.parametri.forEach((param) => {
      const groupName = param.gruppo || null;

      if (groupName) {
        finalizeUngrouped();

        if (currentGroupName === groupName) {
          currentGroupParams.push(param);
        } else {
          finalizeGroup();
          currentGroupName = groupName;
          currentGroupParams = [param];
        }
      } else {
        finalizeGroup();
        ungroupedParams.push(param);
      }
    });

    finalizeGroup();
    finalizeUngrouped();

    this._sections = sections;
  }

  trackBySection(index: number, section: RenderSection): string {
    return section.type + (section.groupName || '') + index;
  }

  trackByParam(index: number, param: ReportParameter): string {
    return param.nome;
  }

  getParametroMetadata(nome: string): ReportParameter | undefined {
    return this.reportInfo?.parametri.find(p => p.nome === nome);
  }

  resolveComponent(param: ReportParameter): any {
    const templateName = this.reportInfo?.template;
    const tplCfg = templateName ? TEMPLATE_CONFIGS[templateName] : undefined;

    if (tplCfg?.widgetOverrides) {
      const widget = findMatch(tplCfg.widgetOverrides, param.nome);
      if (widget) return widget;
    }

    const globalWidget = findMatch(GLOBAL_WIDGET_MAP, param.nome);
    if (globalWidget) return globalWidget;

    return this.getStandardWidget(param.tipo);
  }

  private getStandardWidget(tipo: string): any {
    const map: Record<string, any> = {
      'TEXT': TextWidgetComponent,
      'NUMBER': NumberWidgetComponent,
      'DATE': DateWidgetComponent,
      'COMBO': ComboWidgetComponent,
      'RADIO': RadioWidgetComponent,
      'CHECKBOX': CheckboxWidgetComponent,
    };
    return map[tipo] || TextWidgetComponent;
  }

  getWidgetInputs(param: ReportParameter): Record<string, any> {
    const metadata = this.getParametroMetadata(param.nome);
    const inputs: Record<string, any> = {
      label: this.getParamLabel(param),
      required: metadata?.obbligatorio || false,
      disabled: false,
      control: this.reportForm?.get(param.nome),
    };

    if (param.tipo === 'COMBO' || param.tipo === 'RADIO') {
      inputs['options'] = this.getOptionsForParam(param.nome);
    }

    return inputs;
  }

  getParamCssClass(param: ReportParameter): string | null {
    const templateName = this.reportInfo?.template;
    const tplCfg = templateName ? TEMPLATE_CONFIGS[templateName] : undefined;
    if (!tplCfg?.cssClassOverrides) return null;
    return findMatch(tplCfg.cssClassOverrides, param.nome);
  }

  isParamHidden(param: ReportParameter): boolean {
    if (param.hidden) return true;
    const templateName = this.reportInfo?.template;
    const tplCfg = templateName ? TEMPLATE_CONFIGS[templateName] : undefined;
    return matchesAny(tplCfg?.hiddenParams, param.nome);
  }

  getParamLabel(param: ReportParameter): string {
    const templateName = this.reportInfo?.template;
    const tplCfg = templateName ? TEMPLATE_CONFIGS[templateName] : undefined;
    const label = tplCfg?.labelOverrides
      ? findMatch(tplCfg.labelOverrides, param.nome)
      : null;
    return label ?? (this.getParametroMetadata(param.nome)?.etichetta || param.nome);
  }

  private buildParametriConDisplay(): { [key: string]: any } {
    const raw = this.reportForm.value || {};
    const enriched: { [key: string]: any } = {};

    for (const key of Object.keys(raw)) {
      enriched[key] = raw[key];
      const meta = this.reportInfo?.parametri.find(p => p.nome === key);
      if (meta?.valoriPossibili && raw[key] != null) {
        const opt = meta.valoriPossibili.find(o => String(o.valore) === String(raw[key]));
        if (opt?.etichetta) {
          enriched[key + '__display'] = opt.etichetta;
        }
      }
    }

    return enriched;
  }

  generaReportSync(): void {
    if (this.reportForm.invalid) {
      this.reportForm.markAllAsTouched();
      this.error = 'Compilare tutti i campi obbligatori';
      return;
    }

    this.submitting = true;
    this.error = null;
    this.successMessage = null;

    const request: GenerateRequest = {
      rptdesign: this.reportInfo.template,
      format: this.selectedFormato,
      parameters: this.buildParametriConDisplay()
    };

    const parameterTypes: { [key: string]: string } = {};
    if (this.reportInfo.parametri) {
      this.reportInfo.parametri.forEach(p => {
        parameterTypes[p.nome] = p.tipo || 'TEXT';
      });
    }

    console.log('Generazione report sincrono:', request);
    console.log('Tipi parametri:', parameterTypes);

    this.reportService.generaReportSync(request, parameterTypes).subscribe({
      next: (blob) => {
        console.log('Report generato con successo:', blob.size, 'bytes');

        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `report_${new Date().toISOString().split('T')[0]}.${this.selectedFormato}`;
        link.click();
        window.URL.revokeObjectURL(url);

        this.successMessage = 'Report generato e scaricato con successo';
        this.submitting = false;
      },
      error: (err) => {
        console.error('Errore nella generazione report:', err);

        let errorMessage = 'Errore nella generazione del report.';

        if (err.status === 400) {
          errorMessage = 'Parametri non validi. Verificare i dati inseriti.';
        } else if (err.status === 500) {
          errorMessage = 'Errore interno del server. Contattare l\'amministratore di sistema.';
        } else if (err.status === 0) {
          errorMessage = 'Impossibile raggiungere il server. Verificare che il backend sia in esecuzione.';
        }

        this.error = errorMessage;
        this.submitting = false;
      }
    });
  }

  generaReportAsync(): void {
    if (this.reportForm.invalid) {
      this.reportForm.markAllAsTouched();
      this.error = 'Compilare tutti i campi obbligatori';
      return;
    }

    this.submitting = true;
    this.error = null;
    this.successMessage = null;

    const request: GenerateRequest = {
      rptdesign: this.reportInfo.template,
      format: this.selectedFormato,
      parameters: this.buildParametriConDisplay()
    };

    const parameterTypes: { [key: string]: string } = {};
    if (this.reportInfo.parametri) {
      this.reportInfo.parametri.forEach(p => {
        parameterTypes[p.nome] = p.tipo || 'TEXT';
      });
    }

    console.log('Generazione report asincrono:', request);
    console.log('Tipi parametri:', parameterTypes);

    this.reportService.generaReportAsync(request, parameterTypes).subscribe({
      next: (response) => {
        console.log('Report asincrono avviato:', response);
        this.successMessage = `Report avviato in elaborazione. ID: ${response.reportId}. Sarà disponibile nella ricerca report.`;
        this.submitting = false;
      },
      error: (err) => {
        console.error('Errore nell\'avvio report asincrono:', err);

        let errorMessage = 'Errore nell\'avvio della generazione asincrona.';

        if (err.status === 400) {
          errorMessage = 'Parametri non validi. Verificare i dati inseriti.';
        } else if (err.status === 409) {
          errorMessage = 'Coda elaborazioni piena. Riprovare più tardi.';
        } else if (err.status === 500) {
          errorMessage = 'Errore interno del server. Contattare l\'amministratore di sistema.';
        } else if (err.status === 0) {
          errorMessage = 'Impossibile raggiungere il server. Verificare che il backend sia in esecuzione.';
        }

        this.error = errorMessage;
        this.submitting = false;
      }
    });
  }

  indietro(): void {
    this.router.navigate(['/report']);
  }

  annulla(): void {
    this.error = null;
    this.successMessage = null;
    if (this.reportInfo?.parametri) {
      this.selectedFormato = 'pdf';
      this.computeSections();
      this.creaForm(this.reportInfo.parametri);
    }
  }
}
