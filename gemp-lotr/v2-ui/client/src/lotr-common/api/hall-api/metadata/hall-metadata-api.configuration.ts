import { AxiosRequestConfig } from 'axios';
import { HeartbeatApiParameters } from '../heartbeat-api-parameters.interface';

export const getHallMetadata = (
  parameters: HeartbeatApiParameters
): AxiosRequestConfig<HeartbeatApiParameters> => {
  return {
    method: 'GET',
    headers: { 'Content-Type': 'application/xml' },
    data: parameters,
    url: `/gemp-lotr-server/hall/`,
  };
};
