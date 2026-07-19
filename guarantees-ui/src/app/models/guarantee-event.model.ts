export interface GuaranteeChangeEvent {
  guaranteeId: number;
  action: string;
  reference: string;
  status: string;
  occurredAt: string;
}

export interface ExportReadyEvent {
  jobId: string;
  status: string;
  downloadUrl: string;
  message?: string;
}

export interface GuaranteeEvent {
  type?: string; // 'expiration-auto' or other types
  eventType?: string; // 'expiration-auto' (from backend payload discriminator)
  guaranteeId: string;
  reference: string;
  status: string;
  expiryDate: string;
  reason?: string;
  expiredAt?: string;
}
