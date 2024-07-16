FROM node:22-alpine3.18

COPY package.json /app/

WORKDIR /app

RUN npm install

COPY . .

CMD ["npm", "run", "dev"]