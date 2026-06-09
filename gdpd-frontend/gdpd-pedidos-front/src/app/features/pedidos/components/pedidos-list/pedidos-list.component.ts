import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Subject, BehaviorSubject } from 'rxjs';
import { takeUntil, debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
import { PedidosService } from '../../services/pedidos.service';
import { NotificacionesService } from '../../../../core/services/notificaciones.service';
import { Pedido, EstadoPedido, PageResponse, PedidoFilter } from '../../models/pedido.model';

@Component({
  selector: 'app-pedidos-list',
  templateUrl: './pedidos-list.component.html',
  styleUrls: ['./pedidos-list.component.scss']
})
export class PedidosListComponent implements OnInit, OnDestroy {
  pedidos: Pedido[] = [];
  totalElements = 0;
  isLoading = false;
  error: string | null = null;

  filter: PedidoFilter = { page: 0, size: 10, sort: 'fechaCreacion,desc' };
  estadoOpciones: Array<{ value: EstadoPedido | ''; label: string }> = [
    { value: '', label: 'Todos los estados' },
    { value: 'PENDIENTE',   label: 'Pendiente' },
    { value: 'PROCESANDO',  label: 'Procesando' },
    { value: 'ENVIADO',     label: 'Enviado' },
    { value: 'ENTREGADO',   label: 'Entregado' },
    { value: 'CANCELADO',   label: 'Cancelado' }
  ];

  private reload$ = new BehaviorSubject<PedidoFilter>(this.filter);
  private destroy$ = new Subject<void>();

  constructor(
    private pedidosService: PedidosService,
    private notificacionesService: NotificacionesService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.reload$.pipe(
      debounceTime(100),
      distinctUntilChanged((a, b) => JSON.stringify(a) === JSON.stringify(b)),
      switchMap(f => {
        this.isLoading = true;
        this.error = null;
        return this.pedidosService.getPedidos(f);
      }),
      takeUntil(this.destroy$)
    ).subscribe({
      next: (page: PageResponse<Pedido>) => {
        this.pedidos = page.content;
        this.totalElements = page.totalElements;
        this.isLoading = false;
      },
      error: () => {
        this.error = 'Error al cargar los pedidos. Inténtelo de nuevo.';
        this.isLoading = false;
      }
    });

    // Reactive update on SSE events
    this.notificacionesService.eventos$.pipe(
      takeUntil(this.destroy$)
    ).subscribe(() => {
      this.reload$.next({ ...this.filter });
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  onEstadoChange(estado: string): void {
    this.filter = { ...this.filter, page: 0, estado: estado as EstadoPedido || undefined };
    this.reload$.next(this.filter);
  }

  onPageChange(page: number): void {
    this.filter = { ...this.filter, page };
    this.reload$.next(this.filter);
  }

  verDetalle(id: number): void {
    this.router.navigate(['/pedidos', id]);
  }

  nuevoPedido(): void {
    this.router.navigate(['/pedidos', 'nuevo']);
  }

  get totalPages(): number {
    return Math.ceil(this.totalElements / (this.filter.size ?? 10));
  }

  get currentPage(): number {
    return this.filter.page ?? 0;
  }
}
