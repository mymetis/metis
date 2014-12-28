
[  ](id:top)

- [Introduction](#Introduction)
- [Metis API](#Metis-API)
- [License](#License)
- [Packaging](#Packaging)
- [Configuration](#Configuration)
  - [Deployment Descriptor](#Deployment-Descriptor)
    - [\<servlet\> Tag](#servlet)
    - [\<servlet-mapping\> Tag](#servlet-mapping)
    - [\<distributable\> Tag](#distributable)
    - [\<security-constraint\> Tag](#security-constraint)
  - [Web Application Context](#Web-Application-Context)
    - [RdbMapper](#RdbMapper)
    - [WdsResourceBean](#WdsResourceBean)
      - [sqls4Get](#sqls4Get)
      - [sqls4Put](#sqls4Put)
      - [sqls4Post](#sqls4Post)
      - [sqls4Delete](#sqls4Delete)
      - [secure](#secure)

<br>

<h2 id="Introduction">Introduction</h2>
[[back to top]](#top)

Representational State Transfer (REST) was originally conceived by Roy Fielding as an architecture style for distributed hypermedia. The World Wide Web (W<sup>3</sup>) is based on this style and it is the largest system that conforms to the REST principles. Roy Fielding is also one of the principal authors of  versions 1.0 and 1.1 of the HyperText Transfer Protocol (HTTP). REST was developed alongside these versions of HTTP, and version 1.1 was designed to conform to the REST style. This [Wikipedia page](http://en.wikipedia.org/wiki/Representational_state_transfer) provides a good synopsis on REST, as well as links to excellent reference material on the topic.

REST describes clients and servers, with clients initiating service requests to servers, and servers processing those requests and returning the appropriate service responses. Another way of viewing this is that servers publish services and clients consume those services. The requests and responses are centered on the manipulation of HTTP accessible resources through their representations. A resource is any logical and meaningful concept that may be addressed. On the W<sup>3</sup>, a representation of a resource is typically an HTML web page that captures the current state of that resource and is addressed by a Uniform Resource Identifier (URI) or  Uniform Resource Locator (URL). With respect to Metis, a resource is an entity (e.g., a student, a building, a classroom, etc.) object that is maintained in a data store or datasource (e.g., a database management system). For example, a student is an entity that is represented by a student database table and an instance of a student is maintained as a row in that table. 

[[back to top]](#top)

A Metis API comprises a set of HTTP request messages and their corresponding response messages. The signature of a particular request message, within an API, is represented by a base URL with optional parameters. For example, the following two URLs can represent API request messages that request information from a data store for a student whose student identification number is 123.
<h2 id="License">License</h2>
[[back to top]](#top)

Metis is provided under the [Apache License v2.0](http://www.apache.org/licenses/LICENSE-2.0). 


<h2 id="Packaging">Packaging</h2>
[[back to top]](#top)

The Metis core product files (i.e., configuration and binary files) are packaged and deployed as a Web ARchive (WAR) file. The standard directory structure of a WAR is mainly comprised of the root directory (/) and its WEB-INF subdirectory. With the exception of the WEB-INF subdirectory, everything in the root directory is publicly accessible; it is a violation of the JEE specification to make the WEB-INF subdirectory and its contents publicly available. [This web page](http://docs.oracle.com/javaee/7/tutorial/doc/packaging003.htm) provides a good description of the Web ARchive and its directory structure:
 





[[back to top]](#top)

<servlet>
    to make requests to this servlet. All names preceded with a ‘!’ will be disallowed.  
    The elements of this list are compared to the value of the User-Agent HTTP request 
    header. Please see the examples described immediately following this sample
    response header. The default setting is “no-store, no-transform”. 
 The *agentNames* init-param is used as a control access mechanism. Here are some examples:


[[back to top]](#top)


```


   selects the servlet.
   directory at a time, using the ’/’ character as a path separator. The longest match determines the servlet selected.
   servlet that handles requests for the extension. An extension is defined as the part of the last segment after 
   the last ’.’ character.
   appropriate for the resource requested. If a "default" servlet is defined for the application, it will be used. 
   Many containers provide an implicit default servlet for serving content.

/baz/*	     | servlet2

<h4 id="distributable"> &lt;distributable&gt; Tag</h4>










```xml
  













:----------------|:------------------------:
.../parking?id=24	|1
.../parking?id=24&first=john&last=smith	|3
.../parking?id=24&first=john	|2
.../parking/space/23	|4
.../parking/spaces/23	|4

:----------------|:------------------------:

Unlike the function statement, the stored procedure statement is not in the form of an assignment statement.  By default, the optional parameterized fields, found within the parentheses,  also have a mode of IN. So leaving the mode out simply marks the field as an IN parameter. Unlike the function statement, the parameters within the parentheses can be assigned a mode of OUT or INOUT. The INOUT mode specifies that the field is both an IN and OUT parameter. 


```
```
















