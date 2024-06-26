import { Add, Home } from '@mui/icons-material';
import { Button, Grid } from '@mui/joy';
import { ReactElement } from 'react';
import { Link } from 'react-router-dom';

const deckManagerActions: {
  title: string;
  icon: ReactElement;
  href: string;
}[] = [
  {
    title: 'New Deck',
    icon: <Add />,
    href: '/new',
  },
  {
    title: 'Open Deck',
    icon: <Home />,
    href: '/list',
  },
];

export default function DeckManagerMenu() {
  return (
    <Grid>
      <Grid sx={{ justifyContent: 'center' }} container spacing={2}>
        {deckManagerActions.map((action) => {
          return (
            <Grid key={action.title}>
              <Button
                startDecorator={action.icon}
                variant="soft"
                color="primary"
              >
                <Link to={`/decks${action.href}`}>{action.title}</Link>
              </Button>
            </Grid>
          );
        })}
      </Grid>
      <Grid>
        <Grid>
          Down here we can show recently modified decks as stored in
          LocalStorage
        </Grid>
      </Grid>
    </Grid>
  );
}
