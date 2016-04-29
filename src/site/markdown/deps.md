# Dependencies

RPM packages can declare various dependency information. The most common one is
the "requires" one, which declares that this packages requires another one.

Also see:

 * <https://docs.fedoraproject.org/ro/Fedora_Draft_Documentation/0.1/html/RPM_Guide/ch-dependencies.html>
 * <http://www.rpm.org/max-rpm/s1-rpm-depend-manual-dependencies.html>

## Dependency types

The RPM builder current does support the following dependency types of RPM:

| Name | Tag | Spec Name | Type | Description |
| ----- | ----- | ---------------- | ------------- |
| Requirement | `<requires>` | `requires` | Complex | Require another package or capability |
| Prerequisite | `<prerequisites>` | `prereq` | Simple | Require another package or capability |
| Provide capability | `<provides>` | `provides` | Simple | Provide a capability (aka virtual package) |
| Conflict | `<conflicts>` | `conflicts` | Simple | Conflict with another package |
| Obsoletes | `<obsoletes>` | `obsoletes` | Simple | Obsolete another package |

Dependency declarations come in two types: complex and simple. Currently only the `<requires>` element
allows the complex configuration. The `<prerequisites>` dependency is actually a `<requires>`
dependency, with the `PREREQ` flag set. So this can either be configured using a complex configuration using `<requires>` or the simple configuration using `<prerequisite>`. Both ways will get merged together when the RPM file is being created. 

For more details and the semantics of these dependencies please consult the RPM documentation.

**Note:** In the following examples `<dependencies>` and `<dependency>` will be used to represent any dependency type. 

## Basic configuration

Scripts are configured using the normal plugin configuration:

    <configuration>
      …
        <requires>
            <require>…</require>
            <require>…</require>
        </requires>
      …
    </configuration>

Please note that for each declared dependency a new sub-element without the trailing "s" is required. 

## Simple configuration

Declare a dependencies as follows:

    <dependency>other-package &gt;= 1.0</dependency>
    
Or using text operators in order to avoid XML escaping:

    <dependency>
       other-package ge 1.0   <!-- same as &gt;= -->
    </dependency>

| Operator | XML | Text alias | Description |
| --- | --- | --- | --- |
| =   | `=`   | `eq`  | Equal |
| <   | `&lt;` | `lt`  | Less than |
| >   | `&gt;` | `gt`  | Greater than |
| <=  | `&lt;=` | `le`  | Less than or equal |
| >=  | `&gt;=` | `ge`  | Greater than or equal |

**Note:** The whitespaces between the name and the operate are required. In other words `foo >= 1.0` works, while `foo>=1.0` _does not_! 

**Note:** It is also not possible to declare multiple dependencies in one element: `<provide>foo, bar</provide>`. This will fail the build.

## Complex configuration

Complex dependencies are done in the following way:

    <dependencies>
      <dependency>
        <name>dependency-name</name>   <!--- required, e.g. package name -->
        <version>1.0</version>         <!-- optional version -->
        
        <!-- either one of those if a version was specified -->
        <greater/>
        <greaterOrEqual/>
        <equal/>
        <lessOrEqual/>
        <less/>
        
        <pre/> <!-- optionally : set PREREQ flag -->
        
        <!-- optionally a set of flags -->
        
        <flags>
            <flag>PREREQ</flags>
        </flags>
        
        <!-- or -->
        
        <flags>PREREQ</flags>
        
      </dependency>
    </dependencies>
    
Using the comparators (e.g. `<greater/>`) sum up. So it is possible to use `<greater/><equal/>` instead
of `<greaterOrEqual/>`. However this also allows to produce illegal combinations like `<less/><greater/>`.

The comparator has to be omitted if there is no version and has to be provided when a version is set. 

The `<flags>` element allows to set arbitrary dependency flags.
**Use this at your own risk!** These flags are added to the comparator flags. For the full list of flags see
[RpmDependencyFlags](apidocs/org/eclipse/packagedrone/utils/rpm/deps/RpmDependencyFlags.html).

The specified flags will be added to the internal dependency information. So the following configuration:

    <depenendency>
      <name>foo</name>
      <version>1.0</version>
      <greater/>
      <flags>
          <flag>PREREQ</flag>
          <flag>EQUAL</flag>
      </flags>
    </dependency>
    
will result in the dependency information: `(pre) foo >= 1.0` 
    
It is also possible to use the simple configuration syntax described above.
    
## Remarks

**Note:** Virtual packages (provides) should not have a version number according to RPM. However this is
not enforced by the RPM builder plugin, but a warning is printed out on the console.
 