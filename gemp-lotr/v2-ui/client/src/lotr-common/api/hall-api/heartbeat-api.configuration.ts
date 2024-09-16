import { AxiosRequestConfig } from 'axios';
import { HeartbeatApiParameters } from './heartbeat-api-parameters.interface';

export const heartbeatApiParameters = (
  parameters: HeartbeatApiParameters
): AxiosRequestConfig<HeartbeatApiParameters> => {
  return {
    method: 'POST',
    headers: { 'Content-Type': 'application/xml' },
    data: parameters,
    url: `/gemp-lotr-server/hall/update`,
  };
};
