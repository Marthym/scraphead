# Scraphead [![](https://img.shields.io/github/release/Marthym/scraphead.svg)](https://GitHub.com/Marthym/scraphead/releases/) [![GitHub license](https://img.shields.io/github/license/Marthym/scraphead.svg)](https://github.com/Marthym/scraphead/blob/master/LICENSE)

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Marthym_scraphead&metric=alert_status)](https://sonarcloud.io/dashboard?id=Marthym_scraphead)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=Marthym_scraphead&metric=coverage)](https://sonarcloud.io/dashboard?id=Marthym_scraphead)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=Marthym_scraphead&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=Marthym_scraphead)

**Scraphead** allow scrapping html from URL in order to retrieve OpenGraph, Twitter Card and many other meta information
from HTML head tag.

## Description

**Scraphead** was divided into `core` and `netty`. The `core` contains all the logic, the HTML head parsing and the
mapping into **OpenGraph** and **Twitter Card** model. The `netty` was one of the multiple possible implementations for
the web client.

### Main features

* non blocking
* download only the `<head/>`, not the entire HTML file
* Multiple web client implementation available
* Detect file encoding
* Read [OpenGraph](https://ogp.me/)
  and [Twitter Card](https://developer.twitter.com/en/docs/twitter-for-websites/cards/guides/getting-started), and more
* Allow plugins for specific treatment *(depending on domain for example)*
* build for Java 17 and modules

## Usage

```xml
<dependency>
    <groupId>fr.ght1pc9kc</groupId>
    <artifactId>scraphead-core</artifactId>
    <version>${scraphead.version}</version>
</dependency>

<dependency>
    <groupId>fr.ght1pc9kc</groupId>
    <artifactId>scraphead-netty</artifactId>
    <version>${scraphead.version}</version>
</dependency>
```