import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Guarantee, CreateGuaranteeRequest, UpdateGuaranteeRequest, Amendment, Claim } from '../models/guarantee.model';

@Injectable({
  providedIn: 'root'
})
export class GuaranteeService {
  private apiUrl = '/api/v1/guarantees';

  constructor(private http: HttpClient) {}

  getAll(page: number = 0, size: number = 10): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}?page=${page}&size=${size}`);
  }

  getById(id: string): Observable<Guarantee> {
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

  addAmendment(id: string, amendment: Amendment): Observable<Guarantee> {
    return this.http.post<Guarantee>(`${this.apiUrl}/${id}/amendments`, amendment);
  }

  addClaim(id: string, claim: Claim): Observable<Guarantee> {
    return this.http.post<Guarantee>(`${this.apiUrl}/${id}/claims`, claim);
  }
}
