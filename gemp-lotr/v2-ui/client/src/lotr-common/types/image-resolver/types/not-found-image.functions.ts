import { notFoundImageUrl, notFoundQueryParam } from './card-image.constants';

export const buildNotFoundUrl = (imageId: string): string => {
  return `${notFoundImageUrl}?${notFoundQueryParam}=${imageId}`;
};
