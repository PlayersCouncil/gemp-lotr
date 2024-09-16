import '@fontsource/public-sans';
import { CssBaseline, CssVarsProvider } from '@mui/joy';
import { RouterProvider } from 'react-router-dom';

import themeOverrides from './types/ThemeCustomizations';
import Routes from './common/providers/Routes.provider';

function App() {
  return (
    <CssVarsProvider theme={themeOverrides} defaultMode="dark">
      <CssBaseline />
      <RouterProvider router={Routes} />
    </CssVarsProvider>
  );
}

export default App;
