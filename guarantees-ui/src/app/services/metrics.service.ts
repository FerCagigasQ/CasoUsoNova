import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MetricsDTO } from '../models/metrics.model';

@Injectable({ providedIn: 'root' })
export class MetricsService {
  private readonly api = '/api/v1/metrics';

  constructor(private http: HttpClient) {}

  getMetrics(): Observable<MetricsDTO> {
    return this.http.get<MetricsDTO>(this.api);
  }
}
