/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Component, OnInit, HostListener } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { Observable, map } from 'rxjs';
import { distinctUntilChanged, filter, startWith } from 'rxjs/operators';
import { BreadcrumbService } from './core/services/breadcrumb.service';
import { Store } from '@ngxs/store';
import { DevAuthenticationService } from './core/services/dev-authentication.service';
import { AzioniService } from './core/services/azioni.service';
import { ThemeService, THEME_PRESETS, ThemePreset } from './core/services/theme.service';
import { PrimanotaService } from './core/services/primanota.service';
import { NotificationService } from './core/services/notification.service';
import { NotificheService, Notifica } from './core/services/notifiche.service';
import { FlussoService } from './core/services/flusso.service';
import { ElaboraFlussoResponseDTO, PrimaNotaConfermaInfo } from './features/flusso/model/flusso.model';
import { PrimaNotaSearchResult } from './features/primanota/models/primanota.model';
import { AuthenticationService } from './core/services/authentication.service';
import { AuthStorageService } from './core/services/auth-storage.service';
import { Select } from '@ngxs/store';
import { ContextState } from './core/store/context/context.state';
import { VERSION_STRING } from '../version';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  standalone: false,
  styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit {
  title = 'accrualwcl-ng';
  version = VERSION_STRING;

  displayName$!: Observable<string | null>;
  username$!: Observable<string | null>;
  utenteCodice$!: Observable<string | null>;
  ruolo$!: Observable<string | null>;
  currentEnte$!: Observable<string | null>;
  currentEnteDenominazione$!: Observable<string | null>;
  faseBilancio$!: Observable<string | null>;
  faseBilancioClass$!: Observable<string>;
  annoBilancio$!: Observable<number | null>;
  availableEnti$!: Observable<any[]>;
  initials$!: Observable<string>;

  // Determina se siamo su una rotta pubblica (nessun header)
  isPublicRoute$!: Observable<boolean>;

  hasBreadcrumbs$!: Observable<boolean>;

  // Notifiche
  incompleteWarning$!: Observable<boolean>;
  incompleteList$!: Observable<PrimaNotaSearchResult[]>;
  incompleteListLoading$!: Observable<boolean>;
  incompleteListError$!: Observable<boolean>;
  incompleteCount$!: Observable<number>;
  incompleteModalOpen = false;
  notifRead = false;
  notifPanelOpen = false;

  // Notifiche persistenti, ricaricate dal backend: sopravvivono al cambio di sessione
  notifiche$!: Observable<Notifica[]>;
  notificheNonLette$!: Observable<number>;

  // Conferma elaborazione flusso
  pendingFlussoConfirmation: ElaboraFlussoResponseDTO | null = null;
  flussoConfirmModalOpen = false;
  flussoConfirmLoading = false;
  elaborationProgress: number | null = null;

  enteDropdownOpen = false;
  settingsOpen = false;
  sidebarCollapsed = false;

  readonly themePresets: ThemePreset[] = THEME_PRESETS;

  constructor(
    private router: Router,
    private store: Store,
    private authService: DevAuthenticationService,
    private authenticationService: AuthenticationService,
    public themeService: ThemeService,
    private primanotaService: PrimanotaService,
    private notificationService: NotificationService,
    private notificheService: NotificheService,
    private flussoService: FlussoService,
    private breadcrumbService: BreadcrumbService,
    private azioniService: AzioniService
  ) {}

  ngOnInit() {
    this.themeService.loadSaved();

    this.displayName$              = this.store.select(ContextState.getDisplayName);
    this.username$                 = this.store.select(ContextState.getUsername);
    this.utenteCodice$             = this.store.select(ContextState.getUtenteCodice);
    this.ruolo$                    = this.store.select(ContextState.getRuolo);
    this.currentEnte$              = this.store.select(ContextState.getCurrentEnte);
    this.currentEnteDenominazione$ = this.store.select(ContextState.getCurrentEnteDenominazione);
    this.faseBilancio$             = this.store.select(ContextState.getFaseBilancio);
    this.annoBilancio$             = this.store.select(ContextState.getAnnoBilancio);
    this.availableEnti$            = this.store.select(ContextState.getAvailableEnti);

    this.initials$ = this.displayName$.pipe(
      map(name => {
        if (!name) return '?';
        return name.split(' ').filter(w => w.length > 0).slice(0, 2)
          .map(w => w[0].toUpperCase()).join('');
      })
    );

    this.faseBilancioClass$ = this.faseBilancio$.pipe(
      map(fase => {
        if (!fase) return '';
        switch (fase.toUpperCase()) {
          case 'APERTO':      return 'fase-aperto';
          case 'CHIUSO':      return 'fase-chiuso';
          case 'PREVISIONE':  return 'fase-restricted';
          case 'PLURIENNALE': return 'fase-restricted';
          default:            return '';
        }
      })
    );

    // Determina se siamo su una rotta pubblica (no header)
    this.isPublicRoute$ = this.router.events.pipe(
      filter(e => e instanceof NavigationEnd),
      map((e: NavigationEnd) => {
        const publicRoutes = ['login', 'reset-password', 'confirm-password-reset', 'change-password', 'session-expired', 'unauthorized'];
        return publicRoutes.some(route => e.urlAfterRedirects.startsWith('/' + route));
      }),
      startWith(false)
    );

    this.hasBreadcrumbs$ = this.breadcrumbService.breadcrumbs$.pipe(
      map(crumbs => crumbs.length > 1)
    );

    // Notifiche prime note - disabilitate all'avvio, verranno attivate dalla pagina di ricerca
    this.incompleteWarning$     = this.primanotaService.incompleteWarning$;
    this.incompleteList$        = this.primanotaService.incompleteList$;
    this.incompleteListLoading$ = this.primanotaService.incompleteListLoading$;
    this.incompleteListError$   = this.primanotaService.incompleteListError$;
    this.incompleteCount$       = this.primanotaService.incompleteList$.pipe(map(l => l.length));

    // Le notifiche vengono dal backend e si ricaricano a ogni cambio di ente: e' il momento in
    // cui prima si perdevano, perche' vivevano solo in memoria.
    this.notifiche$ = this.notificheService.notifiche$;
    this.notificheNonLette$ = this.notifiche$.pipe(map(list => list.filter(n => !n.letta).length));
    this.currentEnte$.pipe(distinctUntilChanged()).subscribe(ente => {
      if (ente) {
        this.notificheService.load().subscribe();
      } else {
        this.notificheService.reset();
      }
    });

    this.flussoService.pendingConfirmation$.subscribe(pending => {
      this.pendingFlussoConfirmation = pending;
      if (pending) {
        this.notifRead = false;
      }
    });

    this.flussoService.elaborationProgress$.subscribe(p => {
      this.elaborationProgress = p;
      if (p !== null) {
        this.notifRead = false;
      }
    });
  }

  // ── Sidebar collapse ────────────────────────────
  onSidebarCollapse(collapsed: boolean): void {
    this.sidebarCollapsed = collapsed;
  }

  // ── Ente dropdown ───────────────────────────────
  toggleEnteDropdown(event: MouseEvent): void {
    event.stopPropagation();
    this.enteDropdownOpen = !this.enteDropdownOpen;
  }

  switchEnte(ente: any, event: MouseEvent): void {
    event.stopPropagation();
    const codice = typeof ente === 'object' ? ente.codice : ente;
    this.authService.setEnte(codice);
    this.enteDropdownOpen = false;
  }

  // ── Settings ────────────────────────────────────
  openSettings(event: MouseEvent): void {
    event.stopPropagation();
    this.settingsOpen = true;
  }

  closeSettings(): void { this.settingsOpen = false; }
  applyPreset(color: string): void { this.themeService.apply(color); }
  onCustomColor(event: Event): void { this.themeService.apply((event.target as HTMLInputElement).value); }
  resetTheme(): void { this.themeService.apply(this.themeService.DEFAULT_COLOR); }

  // ── Pannello notifiche ───────────────────────────
  toggleNotifPanel(event: MouseEvent): void {
    event.stopPropagation();
    this.notifPanelOpen = !this.notifPanelOpen;
  }

  // ── Modale prime note non quadrate ──────────────
  openIncompleteModal(event?: MouseEvent): void {
    event?.stopPropagation();
    this.incompleteModalOpen = true;
    this.notifPanelOpen = false;
    this.notifRead = true;
    this.primanotaService.getIncompleteList().subscribe();
  }

  closeIncompleteModal(): void { this.incompleteModalOpen = false; }

  // ── Notifiche persistenti ───────────────────────
  /**
   * Apre una notifica del centro avvisi. Se porta una richiesta di conferma, riapre la modale
   * usando il token di ripresa salvato a suo tempo: e' quello che permette di rispondere anche
   * in una sessione diversa da quella in cui la richiesta e' nata.
   */
  openNotifica(notifica: Notifica, event?: MouseEvent): void {
    event?.stopPropagation();
    this.notificheService.segnaLetta(notifica.id).subscribe();

    if (notifica.tipo === 'CONFIRM' && notifica.resumeToken) {
      this.pendingFlussoConfirmation = {
        needsConfirmation: true,
        confirmationCode: notifica.codice,
        confirmationMessage: notifica.messaggio,
        resumeToken: notifica.resumeToken
      } as ElaboraFlussoResponseDTO;
      this.notifPanelOpen = false;
      this.flussoConfirmModalOpen = true;
    }
  }

  dismissNotifica(notifica: Notifica, event?: MouseEvent): void {
    event?.stopPropagation();
    this.notificheService.chiudi(notifica.id).subscribe();
  }

  notificaIcon(tipo: Notifica['tipo']): string {
    switch (tipo) {
      case 'ERROR':   return 'error_outline';
      case 'WARN':    return 'warning_amber';
      case 'CONFIRM': return 'help_outline';
      default:        return 'info_outline';
    }
  }

  notificaIconClass(tipo: Notifica['tipo']): string {
    return tipo === 'ERROR' ? 'error' : tipo === 'INFO' ? 'info' : 'warn';
  }

  // ── Modale conferma elaborazione flusso ─────────
  openFlussoConfirmModal(event?: MouseEvent): void {
    event?.stopPropagation();
    this.flussoConfirmModalOpen = true;
    this.notifPanelOpen = false;
    this.notifRead = true;
  }

  closeFlussoConfirmModal(): void {
    this.flussoConfirmModalOpen = false;
  }

  confirmFlussoElaborazione(): void {
    if (!this.pendingFlussoConfirmation?.resumeToken) return;
    const token = this.pendingFlussoConfirmation.resumeToken;
    this.flussoConfirmModalOpen = false;
    this.flussoService.resumeElaborazioneFlusso(token, true).subscribe({
      next: () => {
        this.pendingFlussoConfirmation = null;
        this.notificheService.load().subscribe();
      },
      error: () => this.notificationService.showError('Errore durante la ripresa dell\'elaborazione.', 'Errore')
    });
  }

  refuseFlussoElaborazione(): void {
    if (!this.pendingFlussoConfirmation?.resumeToken) {
      this.flussoService.clearPendingConfirmation();
      this.flussoConfirmModalOpen = false;
      return;
    }
    const token = this.pendingFlussoConfirmation.resumeToken;
    this.flussoConfirmModalOpen = false;
    this.flussoService.resumeElaborazioneFlusso(token, false).subscribe({
      next: () => {
        this.notificationService.showInfo('Elaborazione annullata.', 'Annullato');
        this.pendingFlussoConfirmation = null;
        // Ricarica: la richiesta di conferma si chiude e compare la notifica dell'esito,
        // che restera' nel pannello anche dopo un cambio di sessione.
        this.notificheService.load().subscribe();
      },
      error: () => this.flussoService.clearPendingConfirmation()
    });
  }

  goToPrimaNotaSearch(): void {
    this.closeIncompleteModal();
    this.router.navigate(['/primanota/search']);
  }

  viewPrimaNota(item: PrimaNotaSearchResult): void {
    this.closeIncompleteModal();
    this.router.navigate(['/primanota/dettaglio', item.anno, item.numeroProvvisorio]);
  }

  editPrimaNota(item: PrimaNotaSearchResult): void {
    this.closeIncompleteModal();
    this.router.navigate(['/primanota/modifica', item.anno, item.numeroProvvisorio]);
  }

  validaPrimaNota(item: PrimaNotaSearchResult): void {
    this.primanotaService.validaPrimaNota(item.anno, item.numeroProvvisorio).subscribe({
      next: () => {
        this.notificationService.showSuccess('Prima nota validata con successo');
        this.notifRead = false;
        this.primanotaService.getIncompleteList().subscribe();
        this.primanotaService.checkIncomplete().subscribe();
      },
      error: () => this.notificationService.showError('Errore durante la validazione')
    });
  }

  annullaPrimaNota(item: PrimaNotaSearchResult): void {
    this.primanotaService.annullaPrimaNota(item.anno, item.numeroProvvisorio).subscribe({
      next: () => {
        this.notificationService.showSuccess('Prima nota annullata con successo');
        this.notifRead = false;
        this.primanotaService.getIncompleteList().subscribe();
        this.primanotaService.checkIncomplete().subscribe();
      },
      error: () => this.notificationService.showError('Errore durante l\'annullamento')
    });
  }

  riapriPrimaNota(item: PrimaNotaSearchResult): void {
    this.primanotaService.riapriPrimaNota(item.anno, item.numeroProvvisorio).subscribe({
      next: () => {
        this.notificationService.showSuccess('Prima nota riaperta con successo');
        this.notifRead = false;
        this.primanotaService.getIncompleteList().subscribe();
        this.primanotaService.checkIncomplete().subscribe();
      },
      error: () => this.notificationService.showError('Errore durante la riapertura')
    });
  }

  @HostListener('document:click')
  closeDropdowns(): void {
    this.enteDropdownOpen = false;
    this.notifPanelOpen = false;
  }

  logout(): void {
    this.authenticationService.logout().subscribe({
      complete: () => window.location.href = '/',
      error: () => window.location.href = '/'
    });
  }
  openManual(): void { console.log('Apertura manuale'); }
}
