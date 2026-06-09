import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { GdpdPedidosService } from '../../core/api/gdpd-pedidos.service';
import { PedidoDTO } from '../../core/api/models';

@Component({ selector: 'app-pedido-detail', templateUrl: './pedido-detail.component.html' })
export class PedidoDetailComponent implements OnInit {
  pedido: PedidoDTO | null = null; loading = false; error: string | null = null;
  constructor(private route: ActivatedRoute, private api: GdpdPedidosService, private router: Router) {}
  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) { this.loading = true; this.api.obtenerPedido(Number(id)).subscribe({
      next: (p) => { this.pedido = p; this.loading = false; },
      error: () => { this.error = 'No se pudo cargar.'; this.loading = false; }
    }); }
  }
  goBack(): void { this.router.navigate(['/pedidos']); }
}
