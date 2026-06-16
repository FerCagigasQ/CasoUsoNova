import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  Guarantee, Applicant, Beneficiary, IssuingBank,
  CreateGuaranteeRequest, AmendmentRequest, ClaimRequest, Claim
} from '../models/guarantee.model';

@Injectable({ providedIn: 'root' })
export class GuaranteeService {
  private readonly api = '/api/v1/guarantees';

  constructor(private http: HttpClient) {}

  getAll(status?: string, type?: string): Observable<Guarantee[]> {
    let params = new HttpParams();
    if (status) params = params.set('status', status);
    if (type) params = params.set('type', type);
    return this.http.get<Guarantee[]>(this.api, { params });
  }

  getById(id: number): Observable<Guarantee> {
    return this.http.get<Guarantee>(`${this.api}/${id}`);
  }

  create(req: CreateGuaranteeRequest): Observable<Guarantee> {
    return this.http.post<Guarantee>(this.api, req);
  }

  update(id: number, req: CreateGuaranteeRequest): Observable<Guarantee> {
    return this.http.put<Guarantee>(`${this.api}/${id}`, req);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/${id}`);
  }

  issue(id: number): Observable<Guarantee> {
    return this.http.post<Guarantee>(`${this.api}/${id}/issue`, {});
  }

  addAmendment(id: number, req: AmendmentRequest): Observable<Guarantee> {
    return this.http.post<Guarantee>(`${this.api}/${id}/amendments`, req);
  }

  addClaim(id: number, req: ClaimRequest): Observable<Guarantee> {
    return this.http.post<Guarantee>(`${this.api}/${id}/claims`, req);
  }

  getClaims(id: number): Observable<Claim[]> {
    return this.http.get<Claim[]>(`${this.api}/${id}/claims`);
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
