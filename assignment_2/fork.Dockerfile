FROM debian

MAINTAINER cc-group-19

COPY ./measure-fork.sh ./root

COPY ./forksum.c ./root

WORKDIR ./root

ENTRYPOINT ["/bin/bash", "-c"]

# Run files
RUN apt-get -y update && apt-get -y install gcc
RUN ["chmod", "+x", "measure-fork.sh"]
CMD ["./measure-fork.sh"]
