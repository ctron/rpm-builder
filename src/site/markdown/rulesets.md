# Rulesets

Rulesets are used to select and apply proper entry information
(like owner and file permissions) to the files which are added to
the RPM package.

It is possible to assign this information explicitly when using explicit
directories or files. However using a collector it might better to
provide a set of rules which each file found by the collector will
be checked against.

## Configuring rulesets

Rulesets are configured using the `<rulesets>` configuration element:

    <configuration>
    …
      <rulesets>
        <ruleset>
          <id>my-default</id> <!-- unique identifier -->
          <rules>
            <rule>…</rule>
            <rule>…</rule>
            …
          </rules>
        </ruleset>
        
        <ruleset>
          <id>my-default-2</id> <!-- unique identifier -->
          <rules>
            <rule>…</rule>
            <rule>…</rule>
            …
          </rules>
          <defaultRuleset>my-default</defaultRuleset>
        </ruleset>
      </rulesets>
    …
    </configuration> 

## A ruleset

Each `<ruleset>` does require an `<id>` tag, with a unique id, used for referencing the ruleset.
In addition a `<defaultRuleset>` element can be used to reference another ruleset which will be
used after all rules of the current ruleset haven been processed.

## A rule

Each `<rule>` is a single step in the processing of a ruleset. A rule will only be applied to
the current payload entry if it matches the payload entry. This can be configured using the
`<when>` sub-element:

    <rule>
      <when>
        <prefix>/path/to/file</prefix> <!-- prefix of _target_ file name -->
        <type>…</type> <!-- either "file" or "directory" -->         
      </when>
    </rule>
    
In order for the rule entry to match, _all_ conditions must be matched. Each element
(`<prefix`>, …) may only be specified once for each rule.

If the rule matches the current payload entry, then the provided information will be applied:

    <rule>
      …
      <user>root</user>                   <!-- name of the user -->
      <group>root</group>                 <!-- name of the group -->
      <mode>0644</mode>                   <!-- octal mode -->
      <configuration>true</configuration> <!-- mark as configuration -->
    </rule>
    
Also see [entry information](entry_information.html).

## Ruleset processing

When a ruleset is being processed _all_ rules will be processed. If a rule matches, still all
remaining rules and the parent ruleset, if specified, will be executed.

However a rule can be marked as `<last>` rule, which will terminate processing and also not call
into its default ruleset.

Calling rulesets recursively it not supported and will fail the build.

## Simple example

    <rulesets>
      <ruleset>
        <id>my-default</id>
        
        <rules>
          <rule>
            <user>root</user>
            <group>root</group>
          </rule>
          <rule>
            <when>
              <type>file</file>
              <prefix>/etc/</prefix>
            </when>
            <configuration>true</configuration>
          </rule>
          <rule>
            <when>
              <type>directory</type>
            </when>
            <mode>0755</mode>
          </rule>
          <rule>
            <when>
              <type>file</type>
            </when>
            <mode>0644</mode>
          </rule>
          <rule>
            <when>
              <prefix>/usr/bin/</prefix>
            </when>
            <mode>0755</mode>
          </rule>
        </rules>
        
      </ruleset>
    </rulesets>
    
This example will first set user and group to "root" for all entries. If the target filename starts with `/etc/` _and_ it is a file, then it will mark it as configuration file.
Then it will set the mode to 0755 for directories and 0644 for files. Finally if the target filename
starts with `/usr/bin/` then the file will be mark executable with 0755.