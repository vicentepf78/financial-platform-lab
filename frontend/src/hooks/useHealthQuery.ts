import { useQuery } from '@tanstack/react-query';
import { fetchHealth } from '../api/client';

export function useHealthQuery() {
  return useQuery({
    queryKey: ['health'],
    queryFn: fetchHealth,
  });
}
