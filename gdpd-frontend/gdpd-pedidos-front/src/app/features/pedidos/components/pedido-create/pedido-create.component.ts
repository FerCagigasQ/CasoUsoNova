import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { PedidosService } from '../../services/pedidos.service';

@Component({
  selector: 'app-pedido-create',
  templateUrl: './pedido-create.component.html',
  styleUrls: ['./pedido-create.component.scss']
})
export class PedidoCreateComponent {
  form: FormGroup;
  isSubmitting = false;
  error: string | null = null;

  constructor(
    private fb: FormBuilder,
    private pedidosService: PedidosService,
    private router: Router
  ) {
    this.form = this.fb.group({
      referencia: [
        '',
        [
          Validators.required,
          Validators.minLength(3),
          Validators.maxLength(50),
          Validators.pattern(/^[A-Z0-9\-_]+$/i)
        ]
      ],
      importe: [
        null,
        [Validators.required, Validators.min(0.01), Validators.max(9_999_999.99)]
      ],
      descripcion: [
        '',
        [Validators.required, Validators.minLength(5), Validators.maxLength(500)]
      ]
    });
  }

  get f() {
    return this.form.controls;
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    this.error = null;

    this.pedidosService.createPedido(this.form.value).subscribe({
      next: pedido => {
        this.router.navigate(['/pedidos', pedido.id]);
      },
      error: () => {
        this.error = 'Error al crear el pedido. Verifique los datos e inténtelo de nuevo.';
        this.isSubmitting = false;
      }
    });
  }

  volver(): void {
    this.router.navigate(['/pedidos']);
  }
}
