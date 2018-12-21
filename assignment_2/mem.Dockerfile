FROM debian

MAINTAINER cc-group-19

COPY ./measure-mem.sh ./root

COPY ./memsweep.c ./root

WORKDIR ./root

ENTRYPOINT ["/bin/bash", "-c"]

# Run files
RUN apt-get -y update && apt-get -y install gcc
RUN ["chmod", "+x", "measure-mem.sh"]
CMD ["./measure-mem.sh"]

