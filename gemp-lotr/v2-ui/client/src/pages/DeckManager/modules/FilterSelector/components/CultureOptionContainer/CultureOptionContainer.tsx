import { Box, Typography } from '@mui/joy';
import { useEffect, useState } from 'react';
import GempIcon from '../../../../../../common/components/GempIcon/GempIcon';

interface CultureOptionContainerProps {
  title: string;
  cultures: string[];
  currentSelectedCultures: string[];
  selectionUpdated: (cultures: string[]) => void;
}

export function CultureOptionContainer({
  title,
  cultures,
  selectionUpdated,
  currentSelectedCultures,
}: CultureOptionContainerProps) {
  const [selectedCultures, setSelectedCultures] = useState<string[]>(
    Array.from(new Set(currentSelectedCultures ?? []))
  );

  const selectCulture = (name: string): void => {
    const updated = [...selectedCultures];
    const index = selectedCultures.findIndex((item) => item === name);
    if (index === -1) {
      updated.push(name);
    } else {
      updated.splice(index, 1);
    }
    setSelectedCultures(updated);
  };

  useEffect(() => {
    if (currentSelectedCultures.length === 0 && selectedCultures.length === 0)
      return;
    selectionUpdated(selectedCultures);
  }, [selectedCultures, selectionUpdated, currentSelectedCultures]);
  // TODO: BUG - the way i'm passing the iconClicked prop is causing unnecessary re-renders
  return (
    <Box
      sx={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'flex-start',
        gap: '.5em',
      }}
    >
      <Typography>{title}</Typography>
      <Box sx={{ display: 'flex', flexDirection: 'row', gap: '.4em' }}>
        {cultures
          .sort((a, b) => a.localeCompare(b))
          .map((culture) => {
            return (
              <GempIcon
                key={culture}
                name={`${culture}-culture`}
                selected={selectedCultures.includes(culture)}
                iconClicked={() => selectCulture(culture)}
              />
            );
          })}
      </Box>
    </Box>
  );
}
