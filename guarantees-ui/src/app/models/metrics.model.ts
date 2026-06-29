export interface MonthlyCount {
  month: string;
  count: number;
}

export interface MetricsDTO {
  total: number;
  byStatus: Record<string, number>;
  byType: Record<string, number>;
  byMonth: MonthlyCount[];
}
