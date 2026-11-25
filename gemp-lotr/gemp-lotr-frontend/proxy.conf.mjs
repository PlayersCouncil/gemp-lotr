// Dev-server proxy: everything except the Angular app itself goes to a running Gemp server
// (by default the local Docker instance from gemp-lotr/docker, see its readme).
// Override the target with: GEMP_SERVER=http://host:port npm start
const target = process.env.GEMP_SERVER ?? 'http://localhost:17001';

export default {
  // API calls (deck, collection, delivery, league, ...)
  '/gemp-lotr-server': { target, changeOrigin: false },
  // The dev server prefixes the absolute icon links in index.html with the app's base href; send those to the site's
  // images. The production build keeps them as they are
  '^/gemp-lotr/ng/gemp-lotr/': {
    target,
    changeOrigin: false,
    pathRewrite: { '^/gemp-lotr/ng/gemp-lotr/': '/gemp-lotr/' },
  },
  // The site's other static files (images and so on), but not this app's own path
  '^/gemp-lotr/(?!ng/)': { target, changeOrigin: false },
};
