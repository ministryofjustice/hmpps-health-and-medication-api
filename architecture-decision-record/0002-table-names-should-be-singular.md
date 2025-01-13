[Contents](README.md)
[Next >](9999-end.md)

# 2. Database table names should be singular

Date: 2025-01-10

## Status

✅ Accepted

## Context

Table names can be either singular (i.e. identifying_mark) or plural (i.e. identifying_marks). There are mixed opinions
about which it should be.

## Decision

We are opting to use singular table names.  This is mostly a decision made for consistency, but there are a couple of
other benefits:
* The JPA entities don't need an `@Table` annotation to specify the plural table name
* It keeps all the JPA naming consistent, and we don't need to worry about the oddities of the English language 
such as irregular plurals.

## Consequences

We need to correct some existing tables to conform with this approach.


