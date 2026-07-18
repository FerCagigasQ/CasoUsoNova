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
