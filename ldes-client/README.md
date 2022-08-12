# LDES Client

- [SDK](#sdk)
    - [SDK usage](#sdk-usage)
        - [Service](#service)
        - [ModelConverter](#modelconverter)
    - [SDK configuration](#sdk-configuration)
- [Client CLI](#client-cli)
    - [CLI usage](#cli-usage)
    - [CLI configuration](#cli-configuration)
    - [CLI docker](#cli-docker)

## SDK

This module contains the LDES client SDK that replicates and synchronises an LDES and keeps (non-persisted) state for that process.

Wrappers can call the SDK to do the actual work of scheduling fragment fetching and extracting members.

### SDK usage


#### Service
Call the [LdesClientImplFactory](src/main/java/be/vlaanderen/informatievlaanderen/ldes/client/LdesClientImplFactory.java) to get an instance of the [LdesService](src/main/java/be/vlaanderen/informatievlaanderen/ldes/client/services/LdesServiceImpl.java).

```java
LdesClientImplFactory.getService();
```

This call can be made without arguments, as above, in which case values will be taken from [LdesClientDefaults](src/main/java/be/vlaanderen/informatievlaanderen/ldes/client/LdesClientDefaults.java), or with the following arguments provided:

```java
LdesClientImplFactory.getService(Lang dataSourceFormat);
LdesClientImplFactory.getService(Lang dataSourceFormat, Long expirationInterval);
```

Missing or invalid values will be replaced by values from [LdesClientDefaults](src/main/java/be/vlaanderen/informatievlaanderen/ldes/client/LdesClientDefaults.java).

Once an instance of the [LdesService](src/main/java/be/vlaanderen/informatievlaanderen/ldes/client/services/LdesServiceImpl.java) is obtained, queue the initial fragment and begin processing.

```java
String initialFragmentId = "http://localhost:10101/ldes-test";
LdesService ldesService = LdesClientImplFactory.getService();

ldesService.queueFragment(initialFragmentId);

while (ldesService.hasFragmentsToProcess) {
	LdesFragment fragment = ldesService.processNextFragment();
	
	...
}
```

#### ModelConverter

A ModelConverter is available to convert the Jena models to Strings.

```java
ModelConverter.convertModelToString(Model model);
ModelConverter.convertModelToString(Model model, Lang dataDestinationFormat);
```

When called without specifying the data destination format, the value is taken from [LdesClientDefaults](src/main/java/be/vlaanderen/informatievlaanderen/ldes/client/LdesClientDefaults.java).

- **[org.apache.jena.riot.Lang](https://jena.apache.org/documentation/javadoc/arq/org/apache/jena/riot/Lang.html) dataDestinationFormat**

  The desired output format for the LDES members. This value is force-set on the RDF parsers.
  Default: **n-quads**

### SDK configuration

The SDK can be configured by providing arguments to the [LdesClientImplFactory](src/main/java/be/vlaanderen/informatievlaanderen/ldes/client/LdesClientImplFactory.java).

Accepted arguments are:
- **[org.apache.jena.riot.Lang](https://jena.apache.org/documentation/javadoc/arq/org/apache/jena/riot/Lang.html) dataSourceFormat**

  The expected format of the LDES data source. This value is force-set on the RDF parsers.
  Default: **JSONLD11**

- **[java.lang.Long](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/Long.html) expirationInterval**

  This is the number of seconds to add to the current time before a fragment is considered expired. Only used when the HTTP request that contains the fragment does not have a max-age element in the Cache-control header.
  Default: **604800**


When [LdesClientImplFactory](src/main/java/be/vlaanderen/informatievlaanderen/ldes/client/LdesClientImplFactory.java) is called without these arguments, then default values from [LdesClientDefaults](src/main/java/be/vlaanderen/informatievlaanderen/ldes/client/LdesClientDefaults.java) will be used:

```java
/** The expected RDF format of the LDES data source */
public static final String DEFAULT_DATA_SOURCE_FORMAT = "JSONLD11";

/** The desired RDF format for output */
public static final String DEFAULT_DATA_DESTINATION_FORMAT = "n-quads";

/**
 * The number of seconds to add to the current time before a fragment is considered expired.
 * 
 * Only used when the HTTP request that contains the fragment does not have a max-age element in the Cache-control header.
 */
public static final String DEFAULT_FRAGMENT_EXPIRATION_INTERVAL = "604800";

/**
 * The amount of time to wait to call the LdesService when the queue has no mutable fragments left or when the mutable fragments have not yet expired.
 */
public static final String DEFAULT_POLLING_INTERVAL = "60";
```


## Client CLI

A command-line interface is available that can be started with the base url of the LDES as the single argument.
The stream will be followed and LDES members will be outputted to the console (only once).


### CLI usage

When called without arguments (or with the `-?` flag), the client CLI will print a usage statement.

```bash
java -jar ldes-client-1.0-SNAPSHOT-jar-with-dependencies.jar
```

The client accepts arguments for:
- **Input format (-i or --input-format)**

  Passed on to the client as the [org.apache.jena.riot.Lang](https://jena.apache.org/documentation/javadoc/arq/org/apache/jena/riot/Lang.html) that we expect the LDES data source to be formatted in.
  
- **Output format (-o or --output-format)**

  The desired [org.apache.jena.riot.Lang](https://jena.apache.org/documentation/javadoc/arq/org/apache/jena/riot/Lang.html) for member output. Is passed on to Jena and therefore supports all the formats that Jena supports. Jena's name parser accepts variants on the official RDF format names (e.g. n-quads = nquads)

- **Expiration interval (-e or --expiration)**

  This is the number of seconds to add to the current time before a fragment is considered expired. Only used when the HTTP request that contains the fragment does not have a max-age element in the Cache-control header.
  
- **Polling interval (-p or --polling)**

  This is the number of seconds to wait before polling the client again. When the client does not have any mutable fragments left in the queue, the CLI will wait this amount of seconds before calling the client again.


If invalid values are given or required values are missing (negative or invalid numbers for the intervals or a language identifier that Jena doesn't recognize), the CLI will return with the missing or invalid value, print a help message and exit.

**Example:**

```bash
java -jar ldes-client-1.0-SNAPSHOT-jar-with-dependencies.jar -o turtle http://localhost:10101/ldes-test
```

### CLI configuration

When no commandline arguments are given to the client, defaults from the [properties file](src/main/resources/ldesclientcli.properties) will be used.

```properties
ldes.client.cli.polling.interval=30
ldes.client.cli.fragment.expiration.interval=604800
ldes.client.cli.data.source.format=JSON-LD
ldes.client.cli.data.destination.format=nquads
```
 
When there is no properties file or not all values are set, then the client SDK will use the defaults from [LdesClientDefaults](src/main/java/be/vlaanderen/informatievlaanderen/ldes/client/LdesClientDefaults.java), as explained [previously](#sdk-configuration)

### CLI docker

The CLI is available as a docker container. This removes the need to have a local java environment set up.
Internally, the docker containers calls the CLI with all provided arguments passed on.

**To run:**

```bash
docker run -ti ldes.client [OPTIONS] <FRAGMENT URI>
```

See the [CLI usage](#cli-usage) for available arguments.

**Example**

```bash
docker run -ti ldes.client -o turtle http://localhost:10101
```

**To build:**

```bash
cd ldes-client
docker build -t ldes.client .
```

