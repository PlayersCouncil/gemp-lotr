/** Resize scale for displaying cards */
const cardScale = 357 / 497;

/**  image overrides */
const fixedImages = {
  // "Forth the Three Hunters!" cards, separate special images
  '15_204': 'https://i.lotrtcgpc.net/decipher/LOTR15060D.jpg',
  '15_205': 'https://i.lotrtcgpc.net/decipher/LOTR15060E.jpg',
  '15_206': 'https://i.lotrtcgpc.net/decipher/LOTR15060G.jpg',
  // Holidays Gandalf
  '15_207': 'https://i.lotrtcgpc.net/decipher/LOTR15029H.jpg',
  // Gemp-LotR promos
  gl_theOneRing: '/gemp-lotr/images/cards/gl_theOneRing.png',
};

const rulesImageHref = '/gemp-lotr/images/rules.png';

const foilIndicator = '*';

const tengwarIndicator = 'T';

export const wikiBaseUrl = 'https://wiki.lotrtcgpc.net/wiki';

export const mainImageLocation = 'https://i.lotrtcgpc.net/decipher';

export const defaultImageType = '.jpg';

export const notFoundQueryParam = 'originalImage';

export {
  cardScale,
  fixedImages,
  rulesImageHref,
  foilIndicator,
  tengwarIndicator,
};
