# Jason

Jason is an elastic JSON serializer/deserializer based on Google Gson 2.2.4 code.
For more on Gson visit http://code.google.com/p/google-gson .

## Why?

Gson is a great, performant tool with great architecture. There is one thing missing
though - runtime filtering. Filters (or excluders) need to be defined up front,
before `Gson` object is built. Jason aims at bringing runtime excluders passed
on serialize/deserialize method call.

## Why fork?

Gson is maintained as Google Code project. It used to be a great platform, but does
not keep up with modern standards presented by bitbucket or github. Moreover, original
Gson code is kept in SVN which makes collaborating a pain. It would not be such
a big deal maybe if Gson was alive and kicking. Unfortunately there have been no
new releases for some time now and their official issue tracker is swarmed with
different requests. Maybe at some point in time Jason and Gson will merge, but since
i need this functionality as a part of a bigger OpenSource project, i want to
be independent from Gson developers.

## How to use it?

-- more to come --
