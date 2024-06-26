import { Box, Button } from '@mui/joy';
import { PageInformation } from '../../../../lotr-common/api/collection-api/collection-api-parameters.interface';
import { useCardQueryStore } from '../../../../lotr-common/state/card-filter.state';

type PageSelectorProps = {
  pageUpdated: (update: PageInformation) => void;
};

function PageSelector({ pageUpdated }: PageSelectorProps) {
  const { pageInformation } = useCardQueryStore();

  const nextPage = (event: React.MouseEvent): void => {
    event.preventDefault();
    pageUpdated({
      ...pageInformation,
      start: pageInformation.start + pageInformation.count,
    });
  };

  const previousPage = (event: React.MouseEvent): void => {
    event.preventDefault();
    pageUpdated({
      ...pageInformation,
      start: Math.max(pageInformation.start - pageInformation.count, 0),
    });
  };

  return (
    <Box display="flex" gap={1}>
      <Button variant="soft" onClick={previousPage}>
        Previous
      </Button>
      <Button variant="soft" onClick={nextPage}>
        Next
      </Button>
    </Box>
  );
}

export default PageSelector;
