import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ExportJobStatus {
  jobId: string;
  status: string;
  progress: number;
  downloadUrl?: string;
  message?: string;
}

export interface ExportFilters {
  status?: string;
  type?: string;
}

export interface StartExportResponse {
  jobId: string;
  status: string;
}

@Injectable({ providedIn: 'root' })
export class ExportService {
  private readonly baseUrl = '/api/v1/guarantees/export';

  constructor(private http: HttpClient) {}

  startExcelExport(filters: ExportFilters): Observable<StartExportResponse> {
    return this.http.post<StartExportResponse>(this.baseUrl, {
      format: 'xlsx',
      filters
    });
  }

  getExportStatus(jobId: string): Observable<ExportJobStatus> {
    return this.http.get<ExportJobStatus>(`${this.baseUrl}/${jobId}`);
  }
}
