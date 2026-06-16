// E15: field is "reference" (NOT "number")
// E16: dates are "issueDate" / "expiryDate" (NOT startDate/endDate)
// E17: applicant/beneficiary/issuingBank are OBJECTS (NOT strings)

export interface Guarantee {
  id: number;
  reference: string;
  type: 'PERFORMANCE' | 'ADVANCE_PAYMENT' | 'BID_BOND' | 'WARRANTY';
  amount: number;
  currency: string;
  issueDate: string;
  expiryDate: string;
  status: 'DRAFT' | 'ISSUED' | 'AMENDED' | 'CLAIMED' | 'EXPIRED' | 'CANCELLED';
  applicant: Applicant;
  beneficiary: Beneficiary;
  issuingBank: IssuingBank;
  amendments: Amendment[];
  claims: Claim[];
}

export interface Applicant {
  id: number;
  firstName: string;
  lastName: string;
  taxId: string;
  email: string;
  phone: string;
  address: string;
  country: string;
}

export interface Beneficiary {
  id: number;
  firstName: string;
  lastName: string;
  taxId: string;
  email: string;
  phone: string;
  address: string;
  country: string;
}

export interface IssuingBank {
  id: number;
  name: string;
  bic: string;
  country: string;
}

export interface Amendment {
  id: number;
  amendmentDate: string;
  description: string;
  newAmount: number;
  newExpiryDate: string;
}

export interface Claim {
  id: number;
  claimDate: string;
  claimedAmount: number;
  status: 'SUBMITTED' | 'UNDER_REVIEW' | 'PAID' | 'REJECTED';
  reason: string;
}

// Uses flat IDs to match Java's CreateGuaranteeRequest DTO
export interface CreateGuaranteeRequest {
  reference: string;
  type: string;
  amount: number;
  currency: string;
  issueDate: string;
  expiryDate: string;
  applicantId: number;
  beneficiaryId: number;
  issuingBankId: number;
}

export interface AmendmentRequest {
  newAmount: number;
  newExpiryDate: string;
  description: string;
}

export interface ClaimRequest {
  claimedAmount: number;
  reason: string;
}
