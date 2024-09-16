import { CheckCircle, Error, Warning } from '@mui/icons-material';
import { Tab, Typography } from '@mui/joy';
import { DeckValidityStatus } from '../../../../../common/types/deck/deck-validity-status.type';

interface TabsWithStatusProps {
  tabName: string;
  status: DeckValidityStatus;
  tooltip?: string;
}

export default function TabWithStatus(props: TabsWithStatusProps) {
  const { tabName, status, tooltip } = props;
  let icon = <Warning />;

  if (status === 'ok') {
    icon = <CheckCircle />;
  } else if (status === 'not-started') {
    icon = <Error />;
  }
  return (
    <Tab>
      <Typography>{tabName}</Typography>
      {icon}
    </Tab>
  );
}
