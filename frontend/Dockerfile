# ---- Build ----
FROM node:22-alpine AS build
WORKDIR /app
ENV NG_CLI_ANALYTICS=false
COPY package*.json ./
RUN npm install
COPY . .
RUN npx ng build --configuration production

# ---- Serve ----
FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/linguanotes-web/browser /usr/share/nginx/html
EXPOSE 80
