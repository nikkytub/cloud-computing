FROM nginx:latest

MAINTAINER Nikhil Singh

COPY ./backend.nginx.conf /etc/nginx/nginx.conf
