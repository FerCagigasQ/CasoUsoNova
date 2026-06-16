import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Guarantee, CreateGuaranteeRequest, UpdateGuaranteeRequest, Amendment, Claim, GuaranteeStatus, GuaranteeType } from '../models/guarantee.model';

@Injectable({
  providedIn: 'root'
})
export class GuaranteeService {
  private apiUrl = '/api/v1/guarantees';

  constructor(private http: HttpClient) {}

  getAll(status?: GuaranteeStatus | string, type?: GuaranteeType | string): Observable<Guarantee[]> {
    let params = new HttpParams();
    if (status) params = params.set('status', status);
    if (type) params = params.set('type', type);
    return this.http.get<Guarantee[]>(this.apiUrl, { params });
  }

  getById(id: string | number): Observable<Guarantee> {
    return this.http.get<Guarantee>(`${this.apiUrl}/${id}`);
  }

  create(request: CreateGuaranteeRequest): Observable<Guarantee> {
    return this.http.post<Guarantee>(this.apiUrl, request);
  }

  update(id: string, request: UpdateGuaranteeRequest): Observable<Guarantee> {
    return this.http.put<Guarantee>(`${this.apiUrl}/${id}`, request);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  issue(id: string | number): Observable<Guarantee> {
    return this.http.post<Guarantee>(`${this.apiUrl}/${id}/issue`, {});
  }

  addAmendment(id: string, amendment: Amendment): Observable<Guarantee> {
    return this.http.post<Guarantee>(`${this.apiUrl}/${id}/amendments`, amendment);
  }

  addClaim(id: string, claim: Claim): Observable<Guarantee> {
    return this.http.post<Guarantee>(`${this.apiUrl}/${id}/claims`, claim);
  }
}
