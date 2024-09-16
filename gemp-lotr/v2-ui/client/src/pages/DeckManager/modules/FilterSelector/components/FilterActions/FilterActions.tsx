import { Box, Button } from '@mui/joy';

interface FilterActionProps {
  resetFilter: VoidFunction;
  applyFilters: VoidFunction;
}

export function FilterActions({
  resetFilter,
  applyFilters,
}: FilterActionProps) {
  return (
    <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: '.5em' }}>
      <Button color="info" variant="outlined" onClick={resetFilter}>
        Reset Filters
      </Button>
      <Button color="primary" variant="solid" onClick={applyFilters}>
        Apply Filters
      </Button>
    </Box>
  );
}
