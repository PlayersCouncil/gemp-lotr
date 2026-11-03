// Dev-server proxy: everything except the Angular app itself goes to a running Gemp server
// (by default the local Docker instance from gemp-lotr/docker, see its readme).
// Override the target with: GEMP_SERVER=http://host:port npm start
const target = process.env.GEMP_SERVER ?? 'http://localhost:17001';

export default {
  // API calls (deck, collection, delivery, league, ...)
  '/gemp-lotr-server': { target, changeOrigin: false },
  // The dev server prefixes the absolute legacy stylesheet links in index.html with the app's base href
  // (/gemp-lotr/ng/deckbuilder/gemp-lotr/css/...); send those to the legacy files too. The production build keeps them.
  '^/gemp-lotr/ng/deckbuilder/gemp-lotr/': {
    target,
    changeOrigin: false,
    pathRewrite: { '^/gemp-lotr/ng/deckbuilder/gemp-lotr/': '/gemp-lotr/' },
  },
  // Legacy static files (images, css, legacy js such as PC_Cards.js) but not this app's own path
  '^/gemp-lotr/(?!ng/deckbuilder/)': { target, changeOrigin: false },
};
