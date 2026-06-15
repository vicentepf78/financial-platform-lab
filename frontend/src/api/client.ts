import axios from 'axios';

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
});

export interface ApiEnvelope<T> {
  data: T;
  metadata: Record<string, string>;
}

export interface HealthData {
  status: string;
}

export async function fetchHealth(): Promise<ApiEnvelope<HealthData>> {
  const response = await apiClient.get<ApiEnvelope<HealthData>>('/health');
  return response.data;
}
