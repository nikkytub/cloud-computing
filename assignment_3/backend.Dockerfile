FROM nginx:latest

MAINTAINER Nikhil Singh

VOLUME /var/cache/nginx

COPY ./backend.nginx.conf /etc/nginx/conf.d/default.conf
