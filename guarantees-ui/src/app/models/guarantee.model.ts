export interface IssuingBank {
  id: number;
  name: string;
  bic: string;
  country: string;
}

export interface Applicant {
  id: number;
  firstName: string;
  lastName: string;
  taxId: string;
  email: string;
  phone: string;
  address?: string;
  country?: string;
}

export interface Beneficiary {
  id: number;
  firstName: string;
  lastName: string;
  taxId: string;
  email: string;
  phone: string;
  address?: string;
  country?: string;
}

export interface Amendment {
  id: number;
  amendmentDate: string;
  description: string;
  newAmount?: number;
  newExpiryDate?: string;
}

export interface Claim {
  id: number;
  claimDate: string;
  claimedAmount: number;
  status: 'SUBMITTED' | 'UNDER_REVIEW' | 'PAID' | 'REJECTED';
  reason: string;
}

export type GuaranteeType = 'PERFORMANCE' | 'ADVANCE_PAYMENT' | 'BID_BOND' | 'WARRANTY';
export type GuaranteeStatus = 'DRAFT' | 'ISSUED' | 'AMENDED' | 'CLAIMED' | 'EXPIRED' | 'CANCELLED';

export interface Guarantee {
  id: number;
  reference: string;
  type: GuaranteeType;
  amount: number;
  currency: string;
  issueDate: string;
  expiryDate: string;
  status: GuaranteeStatus;
  applicant: Applicant;
  beneficiary: Beneficiary;
  issuingBank: IssuingBank;
  amendments: Amendment[];
  claims: Claim[];
}

export interface CreateGuaranteeRequest {
  reference: string;
  type: GuaranteeType;
  amount: number;
  currency: string;
  issueDate: string;
  expiryDate: string;
  applicant: { id: number };
  beneficiary: { id: number };
  issuingBank: { id: number };
}
