// Dev-server proxy: everything except the Angular app itself goes to a running Gemp server
// (by default the local Docker instance from gemp-lotr/docker, see its readme).
// Override the target with: GEMP_SERVER=http://host:port npm start
const target = process.env.GEMP_SERVER ?? 'http://localhost:17001';

export default {
  // API calls (deck, collection, delivery, league, ...)
  '/gemp-lotr-server': { target, changeOrigin: false },
  // Legacy static files (images, css, legacy js such as PC_Cards.js) but not this app's own path
  '^/gemp-lotr/(?!ng/deckbuilder/)': { target, changeOrigin: false },
};
