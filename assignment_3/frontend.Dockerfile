FROM nginx:latest

MAINTAINER Nikhil Singh

COPY ./frontend.nginx.conf /etc/nginx/nginx.conf
