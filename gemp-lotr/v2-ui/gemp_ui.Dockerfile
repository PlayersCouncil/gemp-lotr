FROM node:18-alpine
COPY client/package.json client/package.json
WORKDIR /client
RUN npm install
COPY client/ client/
EXPOSE 17001
CMD [ "npm", "run", "dev" ]