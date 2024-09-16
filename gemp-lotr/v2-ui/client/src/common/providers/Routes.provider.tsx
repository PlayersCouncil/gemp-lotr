import { createBrowserRouter } from 'react-router-dom';
import DeckManager from '../../pages/DeckManager/DeckManager';
import GameHall from '../../pages/GameHall/GameHall';
import Login from '../../pages/Landing/components/Login/Login';
import Registration from '../../pages/Landing/components/Registration/Registration';
import Landing from '../../pages/Landing/Landing';
import { gameHallLoader } from '../../pages/GameHall';
import DeckManagerContainer from '../../pages/DeckManager/DeckManagerContainer';
import DeckSelector from '../../pages/DeckManager/components/DeckSelector';
import DeckBuilderContainer from '../../pages/DeckBuilder/DeckBuilderContainer';

export default createBrowserRouter([
  {
    path: '/',
    element: <Landing />,
    children: [
      {
        path: '',
        element: <Login />,
      },
      {
        path: '/register',
        element: <Registration />,
      },
    ],
  },
  {
    path: '/hall',
    element: <GameHall />,
    loader: gameHallLoader,
  },
  {
    path: '/decks',
    children: [
      {
        path: '',
        element: <DeckManagerContainer />,
      },
      {
        path: 'new',
        element: <DeckBuilderContainer />,
      },
      {
        path: 'list',
        element: <DeckSelector />,
      },
    ],
  },
]);
