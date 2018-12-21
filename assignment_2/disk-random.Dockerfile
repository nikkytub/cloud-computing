FROM debian

MAINTAINER cc-group-19

COPY ./measure-disk-random.sh ./root

WORKDIR ./root

ENTRYPOINT ["/bin/bash", "-c"]

# Run files
RUN apt-get -y update && apt-get -y install fio wget
RUN ["chmod", "+x", "measure-disk-random.sh"]
CMD ["./measure-disk-random.sh"]

