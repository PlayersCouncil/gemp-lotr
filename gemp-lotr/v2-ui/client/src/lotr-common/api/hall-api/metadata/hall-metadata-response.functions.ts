import { load } from 'cheerio';
import { HallMetadata } from './hall-metadata.interface';

// TODO: this will be better using extract https://cheerio.js.org/docs/advanced/extract
export const convertGetHallMetadataFromXml = (xml: string): HallMetadata => {
  const data = load(xml);
  return {
    channelNumber: Number(data('hall').attr('channelnumber')),
  };
};
