<!--

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

[![logo](docs/YuritaLogo.png)](https://github.paypal.com/pages/EservDataProcessing/Yurita/)
# Yurita

[![Join the chat at https://gitter.im/pp-yurita](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/pp-yurita?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Build Status](https://travis-ci.org/paypal/yurita.svg?branch=master)](https://travis-ci.org/paypal/yurita)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/4536adca78704f699198a03f9b92a133)](https://app.codacy.com/app/r39132/yurita?utm_source=github.com&utm_medium=referral&utm_content=paypal/yurita&utm_campaign=Badge_Grade_Dashboard)
[![License](https://img.shields.io/badge/License-Apache%202.0-red.svg)](https://opensource.org/licenses/Apache-2.0)
[![Documentation Status](https://readthedocs.org/projects/yurita/badge/?version=latest)](https://yurita.readthedocs.io)


Yurita is an open source project for developing large scale anomaly detection models
[Site](https://github.com/paypal/yurita/)

## Getting Started

### Build from source
```console
foo@bar:~/yurita$ ./gradlew clean build
foo@bar:~/yurita$ ./gradlew publishToMavenLocal
```
### Install from Maven Central

*Please build the project from source at this time or try our dockerized Yurita demo application to build automatically as we make the project jar available on Maven Central in upcoming few days.*

```xml
<dependency>
    <groupId>io.github.paypal</groupId>
    <artifactId>yurita</artifactId>
    <version>1.0.0</version>
</dependency>
```
Other Required Dependencies:
```xml
<dependency>
    <groupId>org.apache.spark</groupId>
    <artifactId>spark-core_2.11</artifactId>
    <version>2.4.1</version>
</dependency>
<dependency>
    <groupId>org.apache.spark</groupId>
    <artifactId>spark-sql_2.11</artifactId>
    <version>2.4.1</version>
</dependency>
```

## Running Dockerized Demo Application

`YuritaSampleApp` directory in the Yurita project root path contains a standalone scala project for you to play around with. Run the demo through Docker inside `YuritaSampleApp` directory as shown below.

### Build Docker Image
```console
foo@bar:~/YuritaSampleApp$ docker build -f Dockerfile -t yuritademo .
```

### Run Docker Container
```console
foo@bar:~/YuritaSampleApp$ docker run -p 8080:8080 -t yuritademo
```

## Writing Your First App
Create SparkSession with your own configurations
```scala
val appName = "AnomalyDetectionAPI"
val sparkConf = new SparkConf().setAppName(appName).setMaster("local[*]")
val spark = SparkSession
    .builder()
    .config(sparkConf)
    .getOrCreate()
```

<br/>

Create dataframe of your data points/attributes with what time interval they occur on
```scala
//sample window timestamp
val window1 = (dateFormat.parse("2011-01-18 01:00:00.0"), dateFormat.parse("2011-01-18 01:00:10.0"))
```
```scala
val inputDF: DataFrame = Seq(
    Person("Ned", "Stark", 40, 40.6, "M", Array(5.5), getTimestamp(window1)),
    Person("Arya", "Stark", 9, 40.1, "F", Array(5.6), getTimestamp(window2)),
    Person("Sansa", "Stark", 13, 46.3, "F", Array(5.6), getTimestamp(window3)),
    Person("Jon Snow", "Stark", 17, 11.4, "M", Array(12.4), getTimestamp(window1),
    ...
).toDF()
```
<br/>

Create a data pipe that will perform specified stastical methods on set columns of dataframe within the window size.
```scala
val categoricalPipe = PipelineBuilder()
    .onColumns(Seq("surname", "gender"))
    .setWindowing(Window.fixed("1 hour"))
    .setWindowReferencing(windowRef)
    .buildCategoricalModel(
    Functions.Categorical.avgRef,
    Functions.Categorical.entropy,
    Functions.statResultThreshold(3.0))
```

Combine multiple pipelines
```scala
val workload = AnomalyWorkload.builder()
    .addAllPipelines(categoricalPipe)
    .addPartitioner("surname")
    .buildWithWatermark("timestamp", "2 hours")
```

Dataset extended api
```scala
df.detectAnomalies(workload).map(_.toString).foreach(println(_))
```

<br/>
Full demo application code can be viewed in our YuritaSampleApp project.

## Contributing to Yurita

Thank you very much for contributing to Yurita. Please read the [contribution guidelines](CONTRIBUTING.md) for the process.

## License

Yurita is licensed under the [Apache License, v2.0](LICENSE.txt)
