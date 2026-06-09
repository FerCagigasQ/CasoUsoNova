import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil, switchMap, filter } from 'rxjs/operators';
import { PedidosService } from '../../services/pedidos.service';
import { NotificacionesService } from '../../../../core/services/notificaciones.service';
import { Pedido } from '../../models/pedido.model';

@Component({
  selector: 'app-pedido-detail',
  templateUrl: './pedido-detail.component.html',
  styleUrls: ['./pedido-detail.component.scss']
})
export class PedidoDetailComponent implements OnInit, OnDestroy {
  pedido: Pedido | null = null;
  isLoading = false;
  error: string | null = null;

  private pedidoId!: number;
  private destroy$ = new Subject<void>();

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private pedidosService: PedidosService,
    private notificacionesService: NotificacionesService
  ) {}

  ngOnInit(): void {
    this.route.paramMap.pipe(
      takeUntil(this.destroy$)
    ).subscribe(params => {
      const id = Number(params.get('id'));
      if (!isNaN(id) && id > 0) {
        this.pedidoId = id;
        this.loadPedido();
      }
    });

    // Refresh on SSE event for this pedido
    this.notificacionesService.eventos$.pipe(
      filter(notif => notif.pedidoId === this.pedidoId),
      takeUntil(this.destroy$)
    ).subscribe(() => {
      this.loadPedido();
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadPedido(): void {
    this.isLoading = true;
    this.error = null;
    this.pedidosService.getPedido(this.pedidoId).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: pedido => {
        this.pedido = pedido;
        this.isLoading = false;
      },
      error: () => {
        this.error = 'Error al cargar el pedido.';
        this.isLoading = false;
      }
    });
  }

  volver(): void {
    this.router.navigate(['/pedidos']);
  }
}
