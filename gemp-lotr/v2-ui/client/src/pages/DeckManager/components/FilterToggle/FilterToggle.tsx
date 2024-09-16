import Button from '@mui/material/Button';
import { Box, IconButton } from '@mui/joy';
import FilterAlt from '@mui/icons-material/FilterAlt';

interface FilterToggleProps {
  showFilters: (e: React.MouseEvent) => void;
}

function FilterToggle({ showFilters }: FilterToggleProps) {
  return (
    <Box sx={{ float: 'right' }}>
      <IconButton
        onClick={showFilters}
        variant="solid"
        sx={{
          position: 'fixed',
          p: 0,
          right: 0,
          minHeight: '8em',
          top: 'calc(50vh - 4em)',
          borderTopRightRadius: '0',
          borderBottomRightRadius: '0',
        }}
      >
        <FilterAlt />
      </IconButton>
    </Box>
  );
}

export default FilterToggle;
