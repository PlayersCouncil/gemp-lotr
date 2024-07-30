import axios from 'axios';
import { LoaderFunction } from 'react-router-dom';
import { convertGetHallMetadataFromXml } from '../../../lotr-common/api/hall-api/metadata/hall-metadata-response.functions';
import { HallMetadata } from '../../../lotr-common/api/hall-api/metadata/hall-metadata.interface';

export const gameHallLoader = (async (): Promise<HallMetadata> => {
  const result = await axios.get(
    '/gemp-lotr-server/hall?participantId=&_=1682313931282',
    {
      headers: {
        'Content-Type': 'application/xml',
      },
    }
  );
  if (result.status === 200) {
    return convertGetHallMetadataFromXml(result.data);
  }
  return {} as HallMetadata;
}) satisfies LoaderFunction;
