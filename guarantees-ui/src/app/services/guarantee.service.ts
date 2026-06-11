import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Guarantee, CreateGuaranteeRequest, Amendment, Claim, Applicant, Beneficiary, IssuingBank } from '../models/guarantee.model';

@Injectable({
  providedIn: 'root'
})
export class GuaranteeService {
  private readonly apiUrl = '/api/v1/guarantees';

  constructor(private http: HttpClient) {}

  getAll(status?: string, type?: string): Observable<Guarantee[]> {
    let params = '';
    if (status) params += `?status=${status}`;
    if (type) params += `${params ? '&' : '?'}type=${type}`;
    return this.http.get<Guarantee[]>(`${this.apiUrl}${params}`);
  }

  getById(id: number): Observable<Guarantee> {
    return this.http.get<Guarantee>(`${this.apiUrl}/${id}`);
  }

  create(request: CreateGuaranteeRequest): Observable<Guarantee> {
    return this.http.post<Guarantee>(this.apiUrl, request);
  }

  update(id: number, request: Partial<CreateGuaranteeRequest>): Observable<Guarantee> {
    return this.http.put<Guarantee>(`${this.apiUrl}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  issue(id: number): Observable<Guarantee> {
    return this.http.post<Guarantee>(`${this.apiUrl}/${id}/issue`, {});
  }

  addAmendment(id: number, amendment: Partial<Amendment>): Observable<Guarantee> {
    return this.http.post<Guarantee>(`${this.apiUrl}/${id}/amendments`, amendment);
  }

  addClaim(id: number, claim: Partial<Claim>): Observable<Guarantee> {
    return this.http.post<Guarantee>(`${this.apiUrl}/${id}/claims`, claim);
  }

  getApplicants(): Observable<Applicant[]> {
    return this.http.get<Applicant[]>('/api/v1/applicants');
  }

  getBeneficiaries(): Observable<Beneficiary[]> {
    return this.http.get<Beneficiary[]>('/api/v1/beneficiaries');
  }

  getIssuingBanks(): Observable<IssuingBank[]> {
    return this.http.get<IssuingBank[]>('/api/v1/issuing-banks');
  }
}
