# YuritaSampleApp

## Build Docker Image
```console
foo@bar:~/YuritaSampleApp$ docker build -f Dockerfile -t yuritademo .
```

## Run Docker Container
```console
foo@bar:~/YuritaSampleApp$ docker run -p 8080:8080 -t yuritademo
```