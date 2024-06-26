import { Box, Typography } from '@mui/joy';

export function EmptyDeckSection() {
  return (
    <Box
      sx={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        height: '100%',
      }}
    >
      <Typography level="h3">Add cards to your deck</Typography>
    </Box>
  );
}
