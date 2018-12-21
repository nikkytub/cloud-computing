FROM debian

MAINTAINER cc-group-19

COPY ./measure-cpu.sh ./root

COPY ./linpack.c ./root

WORKDIR ./root

ENTRYPOINT ["/bin/bash", "-c"]

# Run files
RUN apt-get -y update && apt-get -y install gcc
RUN ["chmod", "+x", "measure-cpu.sh"]
CMD ["./measure-cpu.sh"]

