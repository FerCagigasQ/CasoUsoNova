import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MetricsDTO } from '../models/metrics.model';

@Injectable({ providedIn: 'root' })
export class MetricsService {
  private readonly api = '/api/v1/metrics';

  constructor(private http: HttpClient) {}

  getMetrics(params?: {
    status?: string;
    type?: string;
    currency?: string;
    issueDateFrom?: string;
    issueDateTo?: string;
    expiryDateFrom?: string;
    expiryDateTo?: string;
  }): Observable<MetricsDTO> {
    const cleanParams: Record<string, string> = {};
    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== '') {
          cleanParams[key] = value;
        }
      });
    }
    return this.http.get<MetricsDTO>(this.api, { params: cleanParams });
  }
}
