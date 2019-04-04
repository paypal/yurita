
[![logo](docs/YuritaLogo.png)](https://github.paypal.com/pages/EservDataProcessing/Yurita/)
# Yurita

[![Join the chat at https://gitter.im/paypal/squbs](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/paypal/squbs?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Build Status](https://travis-ci.org/paypal/squbs.svg?branch=master)](https://travis-ci.org/paypal/squbs)
[![Codacy Badge](https://api.codacy.com/project/badge/57368c396cb649c590e4ba678675e55c)](https://www.codacy.com/app/akara-sucharitakul/squbs)
[![Codacy Coverage](https://api.codacy.com/project/badge/coverage/57368c396cb649c590e4ba678675e55c)](https://www.codacy.com/app/akara-sucharitakul/squbs)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.squbs/squbs-unicomplex_2.12/badge.svg?style=flat)](http://search.maven.org/#search|ga|1|g:org.squbs)
[![License](https://img.shields.io/badge/License-Apache%202.0-red.svg)](https://opensource.org/licenses/Apache-2.0)
[![Documentation Status](https://readthedocs.org/projects/squbs/badge/?version=latest)](https://squbs.readthedocs.io)


## Getting Started

## What is Yurita 

Yurita is an open source project for developing large scale anomaly detection models

Yurita's key features include:






## Building Yurita 

To build Yurita and run a clean build run: 

    ./gradlew clean build
    
To build Yurita from a source release, it is first necessary to download the gradle wrapper script above. This bootstrapping process requires Gradle to be installed on the source machine.  Gradle is available through most package managers or directly from [its website](http://www.gradle.org/).  To bootstrap the wrapper, run:

    gradle -b bootstrap.gradle

After the bootstrap script has completed, the regular gradlew instructions below are available.

## Testing Yurita

To run all tests:

    ./gradlew clean test

## Contributing to Yurita

Thank you very much for contributing to Yurita. Please read the [contribution guidelines](CONTRIBUTING.md) for the process.

## License

Yurita is licensed under the [Apache License, v2.0](LICENSE.txt)
