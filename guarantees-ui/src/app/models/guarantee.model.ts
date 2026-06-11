export interface Guarantee {
  id: string;
  number: string;
  description: string;
  status: 'ACTIVE' | 'EXPIRED' | 'CANCELLED';
  startDate: string;
  endDate: string;
  amount: number;
  beneficiary: string;
  amendments: Amendment[];
  claims: Claim[];
}

export interface Amendment {
  id: string;
  date: string;
  description: string;
  type: string;
}

export interface Claim {
  id: string;
  date: string;
  amount: number;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  description: string;
}

export interface CreateGuaranteeRequest {
  number: string;
  description: string;
  startDate: string;
  endDate: string;
  amount: number;
  beneficiary: string;
}

export interface UpdateGuaranteeRequest {
  description?: string;
  status?: string;
}
