export interface MonthlyCount {
  month: string;
  count: number;
}

export interface TopBeneficiary {
  beneficiaryId: number;
  firstName: string;
  lastName: string;
  taxId: string;
  guaranteeCount: number;
  totalAmount: number;
}

export interface MetricsDTO {
  total: number;
  byStatus: Record<string, number>;
  byType: Record<string, number>;
  byCurrency: Record<string, number>;
  byMonth: MonthlyCount[];
  totalAmount: number;
  totalAmountByCurrency: Record<string, number>;
  averageAmount: number;
  activeCount: number;
  expiringIn30Days: number;
  topBeneficiaries: TopBeneficiary[];
}
