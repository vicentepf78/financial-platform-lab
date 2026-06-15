import { Alert, Box, Card, CardContent, CircularProgress, Grid2 as Grid, Typography } from '@mui/material';
import { useHealthQuery } from '../hooks/useHealthQuery';

export function DashboardPage() {
  const { data, isLoading, isError } = useHealthQuery();

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Dashboard Financeiro
      </Typography>
      <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
        Backoffice financeiro — scaffold inicial
      </Typography>

      <Grid container spacing={2}>
        <Grid size={{ xs: 12, md: 6 }}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Status da API
              </Typography>
              {isLoading && <CircularProgress size={24} />}
              {isError && (
                <Alert severity="warning">
                  Backend indisponível. Inicie com Docker Compose ou `mvn spring-boot:run`.
                </Alert>
              )}
              {data && (
                <Alert severity="success">
                  API {data.data.status} — {data.metadata.service}
                </Alert>
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
}
