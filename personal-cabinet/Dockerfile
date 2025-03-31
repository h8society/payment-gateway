# ---------------- STAGE 1: BUILD ----------------
FROM node:20-alpine AS build
WORKDIR /app

COPY package.json package-lock.json ./
RUN npm install

COPY . .
RUN npm run build

# ---------------- STAGE 2: RUNTIME ----------------
FROM node:20-alpine AS production
WORKDIR /app
COPY --from=build /app/dist ./dist
RUN npm install -g serve

EXPOSE 5174
CMD ["serve", "-s", "dist", "-l", "5174"]
