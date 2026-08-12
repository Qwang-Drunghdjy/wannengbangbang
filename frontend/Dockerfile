# ============ 构建阶段 ============
# 国内构建若拉取缓慢，可将基础镜像替换为腾讯云内网加速地址，如：
# mirror.ccs.tencentyun.com/library/node:22-alpine
FROM node:22-alpine AS build
WORKDIR /app

# npmmirror 镜像源加速（Q4：如无需加速可删除本行）
RUN npm config set registry https://registry.npmmirror.com

# 先装依赖，利用层缓存
COPY package.json package-lock.json ./
RUN npm ci

# 拷贝源码并构建（vue-tsc 类型检查 + vite build）
COPY . .
RUN npm run build

# ============ 运行阶段 ============
FROM nginx:alpine AS runtime

# envsubst 注入 BACKEND_URL（nginx:alpine 自带 envsubst/gettext）
COPY nginx.conf /etc/nginx/conf.d/default.conf.template
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80

# BACKEND_URL 在微信云托管控制台配置，形如 http://<后端服务名>:8080（不要带路径尾斜杠）
CMD ["sh", "-c", "envsubst '${BACKEND_URL}' < /etc/nginx/conf.d/default.conf.template > /etc/nginx/conf.d/default.conf && nginx -g 'daemon off;'"]
