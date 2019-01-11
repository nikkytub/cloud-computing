FROM nginx:latest

MAINTAINER Nikhil Singh

VOLUME /var/cache/nginx

COPY ./frontend.nginx.conf /etc/nginx/conf.d/default.conf
