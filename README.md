# Hermes : terminology tools, library and microservice.

>
> Hermes:  "Herald of the gods."
>

[![Scc Count Badge](https://sloc.xyz/github/wardle/hermes)](https://github.com/wardle/hermes/)
[![Scc Cocomo Badge](https://sloc.xyz/github/wardle/hermes?category=cocomo&avg-wage=100000)](https://github.com/wardle/hermes/)
[![CircleCI](https://circleci.com/gh/wardle/hermes.svg?style=shield)](https://circleci.com/gh/wardle/hermes)
![GitHub release (latest by date)](https://img.shields.io/github/v/release/wardle/hermes)
[![DOI](https://zenodo.org/badge/293230222.svg)](https://zenodo.org/badge/latestdoi/293230222)
[![Clojars Project](https://img.shields.io/clojars/v/com.eldrix/hermes.svg)](https://clojars.org/com.eldrix/hermes)

Hermes provides a set of terminology tools built around SNOMED CT including:

* a fast terminology service with full-text search functionality; ideal for driving autocompletion in user interfaces
* an inference engine in order to analyse SNOMED CT expressions and concepts and derive meaning
* cross-mapping to and from other code systems
* support for SNOMED CT compositional grammar and the SNOMED CT expression constraint language.

It is designed as both a library for embedding into larger applications and as a standalone microservice.

It is fast, both for import and for use. It imports and indexes the International
and UK editions of SNOMED CT in less than 5 minutes; you can have a server
running seconds after that.

It replaces previous similar tools I wrote in java and golang and is designed to fit into a wider architecture with
identifier resolution, mapping and semantics as first-class abstractions.

Rather than a single monolithic terminology server, it is entirely reasonable to build
multiple services, each providing an API around a specific edition or version of SNOMED CT,
and to use an API gateway to manage client access. `Hermes` is lightweight and designed
to be composed with other services.

It is part of my PatientCare v4 development; previous versions have been operational within NHS Wales
since 2007.

You can have a working terminology server running by typing only a few lines at a terminal. There's no need
for any special hardware, or any special dependencies such as setting up your own elasticsearch or solr cluster.
You just need a filesystem! Many other tools take hours to import the SNOMED data; you'll be finished in less than
10 minutes!

A HL7 FHIR terminology facade is under development : [hades](https://github.com/wardle/hades).
This exposes the functionality available in `hermes` via a FHIR terminology API. This already
supports search and autocompletion using the $expand operation.

# Quickstart

You can have a terminology server running in minutes.
Full documentation is below, but here is a quickstart.

Before you begin, you will need to have Java installed.

### 1. Download hermes

You can choose to run a jar file by downloading a release and running using Java, 
or run from source code using Clojure:

##### Download a release and run using Java

Download the latest release from https://github.com/wardle/hermes/releases
For simplicity, I've renamed the download jar file to 'hermes.jar' for these
examples

Run the jar file using:

```shell
java -jar hermes.jar
```

When run without parameters, you will be given help text.

In all examples below, `java -jar hermes.jar` is equivalent `clj -M:run` and vice versa.

##### Run from source code using Clojure

Install clojure. e.g on Mac OS X:

```shell
brew install clojure
```

Then clone the repository, change directory and run:

```shell
git clone https://github.com/wardle/hermes
cd hermes
clj -M:run
```

When run without parameters, you will be given help text.

In all examples below, `java -jar hermes.jar` is equivalent `clj -M:run` and vice versa.

### 2. Download and install one or more distributions

You will need to download distributions from a National Release Centre.

How to do this will principally depend on your location. 

For more information, see [https://www.snomed.org/snomed-ct/get-snomed](https://www.snomed.org/snomed-ct/get-snomed).
SNOMED provide a [Member Licensing and Distribution Centre](https://mlds.ihtsdotools.org/#/landing).

In the United States, the National Library of Medicine (NLM) has [more information](https://www.nlm.nih.gov/healthit/snomedct/snomed_licensing.html). For example, the SNOMED USA edition is available from [https://www.nlm.nih.gov/healthit/snomedct/us_edition.html](https://www.nlm.nih.gov/healthit/snomedct/us_edition.html).

In the United Kingdom, you can download a distribution from NHS Digital using the [TRUD service](https://isd.digital.nhs.uk).

`hermes` also provides automated downloads. Currently this is only for the UK, because I don't have access to any other national release centre. `hermes` is designed with an extensible system to provide automated downloads, so could quite easily be adapted to support other release centres. 

If you've downloaded a distribution manually, import using one of these commands:

```shell
java -jar hermes.jar --db snomed.db import ~/Downloads/snomed-2021/
```
or
```shell
clj -M:run --db snomed.db import ~/Downloads/snomed-2021/
```

If you're a UK user and want to use automatic downloads, you can do this

```shell
java -jar hermes.jar --db snomed.db install --dist uk.nhs/sct-clinical --dist uk.nhs/sct-drug-ext --api-key trud-api-key.txt --cache-dir /tmp/trud
```
```shell
clj -M:run --db snomed.db install --dist uk.nhs/sct-clinical --dist uk.nhs/sct-drug-ext --api-key trud-api-key.txt --cache-dir /tmp/trud
```

Ensure you have a [TRUD API key](https://isd.digital.nhs.uk/trud3/user/guest/group/0/home).

This will download both the UK clinical edition and the UK drug extension. If you're a UK user, I'd recommend installing both.

You can download a specific edition using an ISO 6801 formatted date:


```shell
java -jar hermes.jar --db snomed.db install --dist uk.nhs/sct-clinical --api-key trud-api-key.txt --cache-dir /tmp/trud --release-date 2021-03-24
java -jar hermes.jar --db snomed.db install --dist uk.nhs/sct-drug-ext --api-key trud-api-key.txt --cache-dir /tmp/trud --release-date 2021-03-24
```
or
```shell
clj -M:run --db snomed.db install --dist uk.nhs/sct-clinical --api-key trud-api-key.txt --cache-dir /tmp/trud --release-date 2021-03-24
clj -M:run --db snomed.db install --dist uk.nhs/sct-drug-ext --api-key trud-api-key.txt --cache-dir /tmp/trud --release-date 2021-03-24
```

These are most useful for building reproducible container images.
You can get a list of available UK versions by simply looking at the TRUD website, or using:

```shell
java -jar hermes.jar available --dist uk.nhs/sct-clinical --api-key trud-api-key.txt --cache-dir /tmp/trud
```
or
```shell
clj -M:run available --dist uk.nhs/sct-clinical --api-key trud-api-key.txt --cache-dir /tmp/trud
```


My tiny i5 'NUC' machine takes 1 minute to import the UK edition of SNOMED CT and a further minute to import the UK
dictionary
of medicines and devices.

### 3. Index and compact

You must index. Compaction is not mandatory, but advisable.

```shell
java -jar hermes.jar --db snomed.db index compact
```
or
```shell
clj -M:run --db snomed.db index compact
```

My machine takes 6 minutes to build the search indices and 20 seconds to compact the database.

### 4. Run a server!

```shell
java -jar hermes.jar --db snomed.db --port 8080 serve
```
or
```shell
clj -M:run --db snomed.db --port 8080 serve
```

You can use [hades](https://github.com/wardle/hades) with the 'snomed.db' index
to give you a FHIR terminology server.

More detailed documentation is included below.

You can use multiple commands at the same time.

For example:
```shell
java -jar hermes.jar --api-key trud-api-key.txt --db snomed.db install uk.nhs/sct-clinical index compact serve 
```
Will download, extract, import, index and compact a database, and then run a server.


# Common questions

### What is the use of `hermes`?

`hermes` provides a simple library, and optionally a microservice, to help
you make use of SNOMED CT.

A library can be embedded into your application; this is easy using Clojure or
Java. You make calls using the API just as you'd use any regular library.

A microservice runs independently and you make use of the data and software
by making an API call over the network.

Like all `PatientCare` components, you can use `hermes` in either way.
Usually, when you're starting out, it's best to use as a library but larger
projects and larger installations will want to run their software components
independently, optimising for usage patterns, resilience, reliability and
rate of change.

Most people who use a terminology run a server and make calls over the network.

### How is this different to a national terminology service?

Previously, I implemented SNOMED CT within an EPR.
Later I realised how important it was to build it as a separate module;
I created terminology servers in java, and then later in golang;
`hermes` is written in clojure. In the UK,  the different health services in England
and Wales have procured a centralised national terminology server. 
While I support the provision of a national terminology server for convenience, 
I think it's important to recognise that it is the *data* that matters most. We 
need to cooperate and collaborate on semantic interoperability, but the software 
services that make use of those data can be centralised or distributed; when I 
do analytics, I can't see me making server round-trips for every check of 
subsumption! That would be silly; I've been using SNOMED for analytics for 
longer than most; you need flexibility in provisioning terminology services. I 
want tooling that can both provide services at scale, while is capable of 
running on my personal computers as well. 

Unlike other available terminology servers, `hermes` is lightweight and has no 
other dependencies except a filesystem, which can be read-only when in 
operation. This makes it ideal for use in situations such as a data pipeline, 
perhaps built upon Apache Kafka - with `hermes`, SNOMED analytics capability can 
be embedded anywhere. 

I don't believe in the idea of uploading codesystems and value sets in place.
My approach to versioning is to run different services; I simply deploy new
services and switch at the API gateway level.

### Localisation

SNOMED CT is distributed across the world. The base distribution is the
International release, but your local distribution will include this together
with local data. Local data will include region-specific language reference
sets.

The core SNOMED API relating to concepts and their meaning is not affected
by issues of locale. Locale is used to derive the synonyms for any given
concept. There should be a single preferred synonym for every concept in a
given language reference set.

When you build a database, the search index caches the preferred synonym using
the locale specified during index creation. If no locale is specified, then
the system default locale will be used. In general, you should specify a
locale that will match the distribution you are importing.

For example, when you are building your search index, you can use:

```shell
java -jar hermes.jar --db snomed.db --locale en-GB index  
```
or 
```shell
clj -M:run --db snomed.db --locale en-GB index  
```

You can specify the requested locale using IETF BCP 47, or by using a special
SNOMED CT defined locale that includes the identifier of the language reference
set you wish to use. I have added BCP 47 matching as an extension in hermes
as the burden of managing which reference sets to use is left to the client
in the SNOMED standard. Hermes tries to provide a set of sane defaults.

Note: the mapping of BCP 47 codes to a language reference set, or set of
language reference sets, is easily modified. If your locale is currently
unsupported, please raise an issue and it can be added easily. The current
map can be found in [impl/language.clj](src/com/eldrix/hermes/impl/language.clj).

Such mapping is simply an extension for convenience and may not be necessary for
you. You can *always* get the preferred synonym given a specific set of language
reference sets but I find it easier to simply use 'en-GB' and let hermes do the
work for me.

### Can I get support?

Yes. Raise an issue, or more formal support options are available on request,
including a fully-managed service.

### Why are you building so many small repositories?

Small modules of functionality are easier to develop, easier to understand,
easier to test and easier to maintain. I design modules to be composable so that I can
stitch different components together in order to solve problems.

In larger systems, it is easy to see code rotting. Dependencies become outdated and
the software becomes difficult to change easily because of software that depend
on it. Small, well-defined modules are much easier to build and are less likely
to need ongoing changes over time; my goal is to need to update modules only
in response to changes in *domain* not software itself.
I aim for an accretion of functionality.

It is very difficult to 'prove' software is working as designed when there are
lots of moving parts.

### What are you using `hermes` for?

I have embedded it into clinical systems; I use it for a fast autocompletion
service so users start typing and the diagnosis, or procedure, or occupation,
or ethnicity, or whatever, pops up. Users don't generally know they're using
SNOMED CT. I use it to populate pop-ups and drop-down controls, and I use it
for decision support to switch functionality on and off in my user interface -
e.g. does this patient have a type of 'x' such as motor neurone disease - as well
as analytics. A large number of my academic publications are as a result of
using SNOMED in analytics.

### What is this graph stuff you're doing?

I think health and care data are and always will be heterogeneous, incomplete and difficult to process.
I do not think trying to build entities or classes representing our domain works at scale; it is
fine for toy applications and trivial data modelling such as e-observations, but classes and
object-orientation cannot scale across such a complete and disparate environment. Instead, I
find it much easier to think about first-class properties - entity - attribute - value - and
use such triples as a way of building and navigating a complex, hierarchical graph.

I am using a graph API in order to decouple subsystems and can now navigate from clinical data
into different types of reference data seamlessly. For example, with the same backend data, I can view
an x.500 representation of a practitioner, or a FHIR R4 Practitioner resource model.
The key is to recognise that identifier resolution and mapping are first class problems within
the health and care domain. Similarly, I think the semantics of reading data are very different to
one of writing data. I cannot shoehorn health and care data into a REST model in which we read and write
to resources representing the type. Instead, just as in real-life, we record event data which can effect change.
In the end, it is all data.

### Is `hermes` fast?

Hermes benefits from the speed of the libraries it uses, particularly [Apache Lucene](https://lucene.apache.org)
and [lmdb](https://www.symas.com/lmdb), and from some fundamental design
decisions including read-only operation and memory-mapped data files. It provides a
HTTP server using the lightweight and reliable [jetty web server](https://www.eclipse.org/jetty/).

I have a small i3 NUC server on my local wifi network, and here is an example of
load testing, in which users are typing 'mnd' and expecting an autocompletion:

```shell
mark@jupiter classes % wrk -c300 -t12 -d30s --latency  'http://nuc:8080/v1/snomed/search?s=mnd'
Running 30s test @ http://nuc:8080/v1/snomed/search?s=mnd
  12 threads and 300 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    40.36ms   19.97ms 565.73ms   92.08%
    Req/Sec   632.19     66.79     0.85k    68.70%
  Latency Distribution
     50%   38.76ms
     75%   45.93ms
     90%   54.09ms
     99%   79.31ms
  226942 requests in 30.09s, 125.75MB read
Requests/sec:   7540.91
Transfer/sec:      4.18MB
```

This uses 12 threads to make 300 concurrent HTTP connections.
On 99% of occasions, that would provide a fast enough response for
autocompletion (<79ms). Of course, that is users typing at exactly the same time,
so a single instance could support more concurrent users than that. Given its design, Hermes is designed to easily scale
horizontally, because you can simply run more servers and load balance across
them. Of course, these data are fairly crude, because in real-life you'll be
doing more complex concurrent calls. In real deployments, I've only needed one instance for hundreds of concurrent
users, but it is nice to know I can scale easily.

### Can I use `hermes` with containers?

Yes. It is designed to be containerised, although I have a mixture of different
approaches in production, including running from source code directly. I would
usually advise creating a volume and populating that with data, and then
permitting read-only access to your service containers. A shared volume can be
memory mapped by multiple running instances and provide high scalability.

There are some examples of [different configurations available](https://github.com/wardle/hermes-docker).

### Can I use `hermes` on Apple Silicon? 

Yes. There are two options: 

##### Option 1. Install an x86 java SDK and run using that (Rosetta). This is fast enough.

For example, you can get a list of installed JDKs:
```shell
$ /usr/libexec/java_home -V

    11.0.17 (arm64) "Amazon.com Inc." - "Amazon Corretto 11" /Users/mark/Library/Java/JavaVirtualMachines/corretto-11.0.17/Contents/Home
    11.0.14.1 (x86_64) "Azul Systems, Inc." - "Zulu 11.54.25" /Users/mark/Library/Java/JavaVirtualMachines/azul-11.0.14.1-1--x86/Contents/Home
```

Choose an SDK and check what we are using
```shell
$ export JAVA_HOME=$(/usr/libexec/java_home -v 11.0.14.1)
$ clj -M -e '(System/getProperty "os.arch")'

"x86_64"

```

##### Option 2. Build the lmdb library for your architecture (ie arm64). This is fastest.

Install the xcode command line tools, if they are not already installed
```shell
xcode-select --install
```

And then download lmdb and build:
```shell
git clone --depth 1 https://git.openldap.org/openldap/openldap.git
cd openldap/libraries/liblmdb
make -e SOEXT=.dylib
mkdir -p ~/Library/Java/Extensions
cp liblmdb.dylib ~/Library/Java/Extensions
```

Once this native library is copied, you can use `hermes` natively using an arm64 based JDK.

```shell
$ export JAVA_HOME=$(/usr/libexec/java_home -v 11.0.17)
$ clj -M -e '(System/getProperty "os.arch")'

"aarch64"
```

It is likely that lmdbjava will include a pre-built lmdb binary for ARM on Mac OS X, so
these steps will become unnecessary.

# Documentation

### A. How to download and build a terminology service

Ensure you have a pre-built jar file, or the source code checked out from github. See below for build instructions.

I'd recommend installing clojure and running using source code but use the pre-built jar file if you prefer.

#### 1. Download and install at least one distribution.

If your local distributor is supported, `hermes` can do this automatically for you.
Otherwise, you will need to download your local distribution(s) manually.

##### i) Use a registered SNOMED CT distributor to automatically download and import

There is currently only support for automatic download and import for the UK,
but other distribution sources can be added if those services provide an API.

You can see distributions that are available for automatic installation:

```shell
java -jar hermes.jar available
```

```shell
clj -M:run available
```

The basic command is:

```shell
clj -M:run --db snomed.db install --dist <distribution-identifier> [properties] 
```

or if you are using a precompiled jar:

```shell
java -jar hermes.jar --db snomed.db install --dist <distribution-identifier> [properties]
```

The distribution, as defined by `distribution-identifier`, will be downloaded
and imported to the file-based database `snomed.db`.

| Distribution-identifier | Description                                        |
|-------------------------|----------------------------------------------------|
| uk.nhs/sct-clinical     | UK SNOMED CT clinical - incl international release |
| uk.nhs/sct-drug-ext     | UK SNOMED CT drug extension - incl dm+d            |

Each distribution might require custom configuration options. 

For example, the UK releases use the NHS Digital TRUD API, and so you need to
pass in the following parameters:

- `--api-key`   : path to a file containing your NHS Digital TRUD api key
- `--cache-dir` : directory to use for downloading and caching releases

For example, these commands will download, cache and install the International
release, the UK clinical edition and the UK drug extension:

```shell
clj -M:run --db snomed.db install uk.nhs/sct-clinical --api-key=trud-api-key.txt --cache-dir=/tmp/trud
clj -M:run --db snomed.db install uk.nhs/sct-drug-ext --api-key=trud-api-key.txt --cache-dir=/tmp/trud
```

`hermes` will tell you what configuration parameters are required:

```shell
java -jar hermes.jar install --dist uk.nhs/sct-clinical --help
```
or
```shell
clj -M:run install --dist uk.nhs/sct-clinical --help
```

##### ii) Download and install SNOMED CT distribution file(s) manually

Depending on where you live in the World, download the most appropriate
distribution(s) for your needs.

In the UK, we can obtain these from [TRUD](https://isd.digital.nhs.uk).

For example, you can download the UK "Clinical Edition", containing the International and UK clinical distributions
as part of TRUD pack 26/subpack 101.

* [https://isd.digital.nhs.uk/trud3/user/authenticated/group/0/pack/26/subpack/101/releases](https://isd.digital.nhs.uk/trud3/user/authenticated/group/0/pack/26/subpack/101/releases)

Optionally, you can also download the UK SNOMED CT drug extension, that contains the dictionary of medicines and
devices (dm+d) is available
as part of TRUD pack 26/subpack 105.

* [https://isd.digital.nhs.uk/trud3/user/authenticated/group/0/pack/26/subpack/105/releases](https://isd.digital.nhs.uk/trud3/user/authenticated/group/0/pack/26/subpack/105/releases)

Once you have downloaded what you need, unzip them to a common directory and
then you can use `hermes` to create a file-based database.

If you are running using the jar file:

```shell
java -jar hermes.jar --db snomed.db import ~/Downloads/snomed-2020
```

If you are running from source code:

```shell
clj -M:run --db snomed.db import ~/Downloads/snomed-2020/
```

The import of both International and UK distribution files takes
a total of less than 3 minutes on my machine.

#### 2. Index

For correct operation, indices are needed for components, search and reference
set membership.

Run

```shell
java -jar hermes.jar --db snomed.db index
```

or

```shell
clj -M:run --db snomed.db index
```

This will build the indices; it takes about 6 minutes on my machine.

#### 3. Compact database (optional).

This reduces the file size and takes 20 seconds.
This is an optional step, but recommended.

```shell
java -jar hermes.jar --db snomed.db compact
```

or

```shell
clj -M:run --db snomed.db compact
```

#### 4. Run a REPL (optional)

When I first built terminology tools, either in java or in golang, I needed to
also build a custom command-line interface in order to explore the ontology.
This is not necessary as most developers using Clojure quickly learn the value
of the REPL; a read-evaluate-print-loop in which one can issue arbitrary
commands to execute. As such, one has a full Turing-complete language (a lisp)
in which to explore the domain.

Run a REPL and use the terminology services interactively. I usually use a
REPL from within my IDE.

```
clj -A:dev
```

#### 5. Get the status of your installed index

You can obtain status information about any index by using:

```shell
clj -M:run --db snomed.db status --format json
```

Result:

```json
{"releases":
 ["SNOMED Clinical Terms version: 20220731 [R] (July 2022 Release)",
  "35.2.0_20221123000001 UK clinical extension",
  "35.3.0_20221221000001 UK drug extension"],
 "locales":["en-GB", "en-US"],
 "components":
 {"concepts":1066649,
  "descriptions":3039223,
  "relationships":7914216,
  "refsets":535,
  "refset-items":13156533,
  "indices":
  {"descriptions-concept":3039223,
   "concept-parent-relationships":4719818,
   "concept-child-relationships":4719818,
   "component-refsets":10444805,
   "associations":1248897,
   "descriptions-search":3039223,
   "members-search":13156533}}}
```
In this example, you can see I have the July 22 International release, with the 
UK clinical and drug extensions from November and December 22 respectively.
Given that these releases have been imported, hermes recognises it can support
the locales en-GB and en-US. For completeness, detailed statistics on 
components and indices are also provided. Additional options are available:

```shell
java -jar hermes.jar --db snomed.db status --help
```
or
```shell
clj -M:run --db snomed.db status --help
```

#### 6. Run a terminology web service

By default, data are returned using json, but you can
request [edn](https://github.com/edn-format/edn) by simply adding "Accept:application/edn" in the request header.

```
java -jar hermes.jar --db snomed.db --port 8080 serve 
```

or

```
clj -M:run --db snomed.db --port 8080 serve
```

There are a number of configuration options for `serve`:

```shell
java -jar hermes.jar serve --help
```
or
```shell
clj -M:run serve --help
```

```shell
Usage: hermes [options] serve

Start a terminology server

Options:
  -d, --db PATH                               Path to database directory
  -p, --port PORT                       8080  Port number
  -a, --bind-address BIND_ADDRESS             Address to bind
      --allowed-origins "*" or ORIGINS        Set CORS policy, with "*" or comma-delimited hostnames
      --allowed-origin "*" or ORIGIN    []    Set CORS policy, with "*" or hostname. ]
      --locale LOCALE                         Locale to use, if different from system
  -h, --help
```

* `--bind-address` is optional. You may want to use `--bind-address 0.0.0.0`
* `--allowed-origins` is optional. You could use `--allowed-origins "*"` or `--allowed-origins example.com,example.net`
* `--allowed-origin example.com --allowed-origin example.net` is equivalent to `--allowed-origins example.com,example.net`.
* `--allowed-origin "*"` is equivalent to `--allowed-origins "*"`
* `--locale` sets the default locale. This is used in building your search index and as a default if clients do not specify their preference. e.g. `--locale=en-GB`

##### Endpoints:

There are a range of endpoints. Here are some examples:

* `/v1/snomed/concepts/24700007` - return basic data about a single concept
* `/v1/snomed/concepts/24700007/descriptions` - returns all descriptions for concept
* `/v1/snomed/concepts/24700007/preferred` : returns preferred description for concept. Use an `Accept-Language` header to choose your locale (see below).
* `/v1/snomed/concepts/24700007/extended` : returns an extended concept 
* `/v1/snomed/concepts/24700007/historical` - returns historical associations for this concept
* `/v1/snomed/concepts/24700007/refsets` - returns refsets to which this concept is a member
* `/v1/snomed/concepts/24700007/map/999002271000000101` - crossmap to alternate codesystem (ICD-10 in this example)
* `/v1/snomed/concepts/24700007/map/991411000000109` - map into a SNOMED subset (the UK emergency unit refset in this example)
* `/v1/snomed/crossmap/999002271000000101/G35X` - cross from an alternate codesystem (ICD-10 in this example)
* `/v1/snomed/search?s=mnd\&constraint=<64572001&maxHits=5` - search for a term, constrained by SNOMED ECL expression
* `/v1/snomed/expand?ecl= <19829001 AND <301867009&includeHistoric=true` - expand SNOMED ECL expression

##### Get a single concept 

```shell
http '127.0.0.1:8080/v1/snomed/concepts/24700007'
```

```json
{
  "active": true,
  "definitionStatusId": 900000000000074008,
  "effectiveTime": "2002-01-31",
  "id": 24700007,
  "moduleId": 900000000000207008
}
```

You'll want to use the other endpoints much more frequently.

##### Get extended information about a single concept


```shell
http 127.0.0.1:8080/v1/snomed/concepts/24700007/extended
```

The result is an extended concept definition - all the information
needed for inference, logic and display. For example, at the client
level, we can then check whether this is a type of demyelinating disease
or is a disease affecting the central nervous system without further
server round-trips. Each relationship also includes the transitive closure tables for that
relationship, making it easier to execute logical inference.
Note how the list of descriptions includes a convenient
`acceptableIn` and `preferredIn` so you can easily display the preferred
term for your locale. If you provide an Accept-Language header, then
you will also get a preferredDescription that is the best choice for those
language preferences given what is installed.

```shell
HTTP/1.1 200 OK
Content-Type: application/json
Date: Mon, 08 Mar 2021 22:01:13 GMT

{
    "concept": {
        "active": true,
        "definitionStatusId": 900000000000074008,
        "effectiveTime": "2002-01-31",
        "id": 24700007,
        "moduleId": 900000000000207008
    },
    "descriptions": [
        {
            "acceptableIn": [],
            "active": true,
            "caseSignificanceId": 900000000000448009,
            "conceptId": 24700007,
            "effectiveTime": "2017-07-31",
            "id": 41398015,
            "languageCode": "en",
            "moduleId": 900000000000207008,
            "preferredIn": [
                900000000000509007,
                900000000000508004,
                999001261000000100
            ],
            "refsets": [
                900000000000509007,
                900000000000508004,
                999001261000000100
            ],
            "term": "Multiple sclerosis",
            "typeId": 900000000000013009
        },
        {
            "acceptableIn": [],
            "active": false,
            "caseSignificanceId": 900000000000020002,
            "conceptId": 24700007,
            "effectiveTime": "2002-01-31",
            "id": 41399011,
            "languageCode": "en",
            "moduleId": 900000000000207008,
            "preferredIn": [],
            "refsets": [],
            "term": "Multiple sclerosis, NOS",
            "typeId": 900000000000013009
        },
        {
            "acceptableIn": [],
            "active": false,
            "caseSignificanceId": 900000000000020002,
            "conceptId": 24700007,
            "effectiveTime": "2015-01-31",
            "id": 41400016,
            "languageCode": "en",
            "moduleId": 900000000000207008,
            "preferredIn": [],
            "refsets": [],
            "term": "Generalized multiple sclerosis",
            "typeId": 900000000000013009
        },
        {
            "acceptableIn": [],
            "active": false,
            "caseSignificanceId": 900000000000020002,
            "conceptId": 24700007,
            "effectiveTime": "2015-01-31",
            "id": 481990016,
            "languageCode": "en",
            "moduleId": 900000000000207008,
            "preferredIn": [],
            "refsets": [],
            "term": "Generalised multiple sclerosis",
            "typeId": 900000000000013009
        },
        {
            "acceptableIn": [],
            "active": true,
            "caseSignificanceId": 900000000000448009,
            "conceptId": 24700007,
            "effectiveTime": "2017-07-31",
            "id": 754365011,
            "languageCode": "en",
            "moduleId": 900000000000207008,
            "preferredIn": [
                900000000000509007,
                900000000000508004,
                999001261000000100
            ],
            "refsets": [
                900000000000509007,
                900000000000508004,
                999001261000000100
            ],
            "term": "Multiple sclerosis (disorder)",
            "typeId": 900000000000003001
        },
        {
            "acceptableIn": [
                900000000000509007,
                900000000000508004,
                999001261000000100
            ],
            "active": true,
            "caseSignificanceId": 900000000000448009,
            "conceptId": 24700007,
            "effectiveTime": "2017-07-31",
            "id": 1223979019,
            "languageCode": "en",
            "moduleId": 900000000000207008,
            "preferredIn": [],
            "refsets": [
                900000000000509007,
                900000000000508004,
                999001261000000100
            ],
            "term": "Disseminated sclerosis",
            "typeId": 900000000000013009
        },
        {
            "acceptableIn": [
                900000000000509007,
                900000000000508004,
                999001261000000100
            ],
            "active": true,
            "caseSignificanceId": 900000000000017005,
            "conceptId": 24700007,
            "effectiveTime": "2003-07-31",
            "id": 1223980016,
            "languageCode": "en",
            "moduleId": 900000000000207008,
            "preferredIn": [],
            "refsets": [
                900000000000509007,
                900000000000508004,
                999001261000000100
            ],
            "term": "MS - Multiple sclerosis",
            "typeId": 900000000000013009
        },
        {
            "acceptableIn": [
                900000000000509007,
                900000000000508004,
                999001261000000100
            ],
            "active": true,
            "caseSignificanceId": 900000000000017005,
            "conceptId": 24700007,
            "effectiveTime": "2003-07-31",
            "id": 1223981017,
            "languageCode": "en",
            "moduleId": 900000000000207008,
            "preferredIn": [],
            "refsets": [
                900000000000509007,
                900000000000508004,
                999001261000000100
            ],
            "term": "DS - Disseminated sclerosis",
            "typeId": 900000000000013009
        }
    ],
    "directParentRelationships": {
        "116676008": [
            409774005,
            32693004
        ],
        "116680003": [
            6118003,
            414029004,
            39367000
        ],
        "363698007": [
            21483005
        ],
        "370135005": [
            769247005
        ]
    },
    "parentRelationships": {
        "116676008": [
            138875005,
            107669003,
            123037004,
            409774005,
            32693004,
            49755003,
            118956008
        ],
        "116680003": [
            6118003,
            138875005,
            404684003,
            123946008,
            118234003,
            128139000,
            23853001,
            246556002,
            363170005,
            64572001,
            118940003,
            414029004,
            362975008,
            363171009,
            39367000,
            80690008,
            362965005
        ],
        "363698007": [
            138875005,
            21483005,
            442083009,
            123037004,
            25087005,
            91689009,
            91723000
        ],
        "370135005": [
            138875005,
            769247005,
            308489006,
            303102005,
            281586009,
            362981000,
            719982003
        ]
    },
    "refsets": [
        991381000000107,
        999002271000000101,
        991411000000109,
        1127581000000103,
        1127601000000107,
        900000000000497000,
        447562003
    ]
}

```

##### Search

Example usage of search endpoint.

```shell
http '127.0.0.1:8080/v1/snomed/search?s=mnd\&constraint=<64572001&maxHits=5'
````

```shell.
[
  {
    "id": 486696014,
    "conceptId": 37340000,
    "term": "MND - Motor neurone disease",
    "preferredTerm": "Motor neuron disease"
  }
]

```

This searches only active concepts, but both active and inactive descriptions, by default. This can be changed
per request. The defaults are sensible, because a user trying to find something with a now inactive synonym
such as 'Wegener's Granulomatosis' will be suprised that their search fails to return any results.

Search parameters:

* `s` - the text to search
* `constraint` - an ECL expression to constrain the search; I never use search without this
* `maxHits` - maximum number of hits
* `inactiveConcepts` - whether to search inactive concepts (default, `false`)
* `inactiveDescriptions` - whether to search inactive descriptions (default, `true`)
* `fuzzy` - whether to use fuzziness for search (default, `false`)
* `fallbackFuzzy` - whether to retry using a fuzziness factor if initial search returns no results (default, `false`)
* `removeDuplicates` - whether to remove results with the same conceptId and text (default, `false`)

For autocompletion, in a typical type-ahead user interface control, you might use `fallbackFuzzy=1` (or
`fallbackFuzzy=true`) and `removeDuplicates=1` (or `removeDuplicates=true`).
That will mean that if a user mistypes one or two characters, they should still get some sensible results.

Here I search for all UK medicinal products with the name amlodipine and populate my autocompletion control using the
results:

```shell
http '127.0.0.1:8080/v1/snomed/search?s=amlodipine\&constraint=<10363601000001109&fallbackFuzzy=true&removeDuplicates=true&maxHits=500'
```

More complex expressions are supported, and no search term is actually needed.

Let's get all drugs with exactly three active ingredients:

```shell
http '127.0.0.1:8080/v1/snomed/search?constraint=<373873005|Pharmaceutical / biologic product| : [3..3]  127489000 |Has active ingredient|  = <  105590001 |Substance|'
```

Or, what about all disorders of the lung that are associated with oedema?

```shell
http -j '127.0.0.1:8080/v1/snomed/search?constraint= <  19829001 |Disorder of lung|  AND <  301867009 |Edema of trunk|'
```

The ECL can be written more concisely:

```shell
http -j '127.0.0.1:8080/v1/snomed/search?constraint= <19829001 AND <301867009'
```

##### Expanding ECL without search

If you are simply expanding an ECL expression without search terms, you can use
the `expand` endpoint.

```shell
http -j '127.0.0.1:8080/v1/snomed/expand?ecl= <19829001 AND <301867009&includeHistoric=true'
```

This has an optional parameter `includeHistoric` which can expand the expansion
to include historical associations. This is very useful in analytics.

As a concept identifier is actually a valid SNOMED ECL expression, you can do this:

```shell
http -j '127.0.0.1:8080/v1/snomed/expand?ecl=24700007&includeHistoric=true'
```

```json

[
    {
        "conceptId": 586591000000100,
        "id": 1301271000000113,
        "preferredTerm": "Multiple sclerosis NOS",
        "term": "Multiple sclerosis NOS"
    },
    {
        "conceptId": 192930001,
        "id": 297181019,
        "preferredTerm": "Multiple sclerosis NOS",
        "term": "Multiple sclerosis NOS"
    },
    {
        "conceptId": 24700007,
        "id": 41398015,
        "preferredTerm": "Multiple sclerosis",
        "term": "Multiple sclerosis"
    }
    ...
]
```

##### Crossmap to and from SNOMED CT

There are endpoints for crossmapping to and from SNOMED.

Let's map one of our diagnostic terms into ICD-10:

- `24700007` is multiple sclerosis.
- `999002271000000101` is the ICD-10 UK complex map reference set.

```shell
http -j 127.0.0.1:8080/v1/snomed/concepts/24700007/map/999002271000000101
```

Result:

```
[
    {
        "active": true,
        "correlationId": 447561005,
        "effectiveTime": "2020-08-05",
        "id": "57433204-2371-5c6f-855f-94ff9dad7ba6",
        "mapAdvice": "ALWAYS G35.X",
        "mapCategoryId": 1,
        "mapGroup": 1,
        "mapPriority": 1,
        "mapRule": "",
        "mapTarget": "G35X",
        "moduleId": 999000031000000106,
        "referencedComponentId": 24700007,
        "refsetId": 999002271000000101
    }
]
```

And of course, we can crossmap back to SNOMED as well:

```shell
http -j 127.0.0.1:8080/v1/snomed/crossmap/999002271000000101/G35X
```

If you map a concept into a reference set that doesn't contain that concept, you'll
automatically get the best parent matches instead.

##### Map a concept into a reference set

You will usually crossmap using a SNOMED CT crossmap reference set, such as those for ICD-10 or OPCS. However, `Hermes` supports
crossmapping a concept into any reference set. You can use this feature in data analytics in order to reduce the dimensionality of your dataset. 

Here we have multiple sclerosis (`24700007`), and we're mapping into the UK emergency unit
reference set (`991411000000109`):

```shell
http -j 127.0.0.1:8080/v1/snomed/concepts/24700007/map/991411000000109
```

The UK emergency unit reference set gives a subset of concepts used for central reporting problems and diagnoses in UK emergency units. 

As multiple sclerosis in that reference set, you'll simply get:

```json
[
  {
    "active": true,
    "effectiveTime": "2015-10-01",
    "id": "d55ce305-3dcc-5723-8814-cd26486c37f7",
    "moduleId": 999000021000000109,
    "referencedComponentId": 24700007,
    "refsetId": 991411000000109
  }
]
```

But what happens if we try something that isn't in that emergency reference set?

Here is 'limbic encephalitis with LGI1 antibodies' (`763794005`). It isn't in
that UK emergency unit reference set:

```shell
http -j 127.0.0.1:8080/v1/snomed/concepts/763794005/map/991411000000109
```

Result:

```json
[
  {
    "active": true,
    "effectiveTime": "2015-10-01",
    "id": "5b3b8cdd-dd02-50e3-b207-bf4a3aa17694",
    "moduleId": 999000021000000109,
    "referencedComponentId": 45170000,
    "refsetId": 991411000000109
  }
]
```

You get a more general concept - 'encephalitis' (`45170000`) that is in the
emergency unit reference set. This makes it straightforward to map concepts
into subsets of terms as defined by a reference set for analytics.

You could limit users to only entering the terms in a subset, but much better to allow clinicians to regard highly-specific granular terms and be able to map to less granular terms on demand.

#### 7. Embed into another application

You can use git coordinates in a deps.edn file, or use maven:

In your `deps.edn` file (make sure you change the commit-id):

```
[com.eldrix.hermes {:git/url "https://github.com/wardle/hermes.git"
                    :sha     "097e3094070587dc9362ca4564401a924bea952c"}
``` 

In your pom.xml:

```
<dependency>
  <groupId>com.eldrix</groupId>
  <artifactId>hermes</artifactId>
  <version>1.0.815</version>
</dependency>
```

# Development

See [/doc/development](/doc/development.md) on how to develop, test, lint, deploy and release `hermes`. 