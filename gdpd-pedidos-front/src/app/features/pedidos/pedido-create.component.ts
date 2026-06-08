import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { GdpdPedidosService } from '../../core/api/gdpd-pedidos.service';

@Component({ selector: 'app-pedido-create', templateUrl: './pedido-create.component.html' })
export class PedidoCreateComponent {
  form: FormGroup; submitting = false; error: string | null = null;
  constructor(private fb: FormBuilder, private api: GdpdPedidosService, private router: Router) {
    this.form = this.fb.group({
      clienteId: ['', [Validators.required]], observaciones: [''],
      productoId: ['', [Validators.required]],
      cantidad: [1, [Validators.required, Validators.min(1)]],
      precioUnitario: [0, [Validators.required, Validators.min(0)]]
    });
  }
  onSubmit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.submitting = true; this.error = null;
    const v = this.form.value as {clienteId:string;observaciones:string;productoId:string;cantidad:number;precioUnitario:number};
    this.api.crearPedido({ clienteId: v.clienteId, observaciones: v.observaciones || undefined,
      lineas: [{ productoId: v.productoId, cantidad: v.cantidad, precioUnitario: v.precioUnitario }]
    }).subscribe({ next: (p) => this.router.navigate(['/pedidos', p.id]),
      error: () => { this.error = 'Error al crear.'; this.submitting = false; }
    });
  }
  cancel(): void { this.router.navigate(['/pedidos']); }
}
