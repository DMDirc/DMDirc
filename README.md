DMDirc
================================================================================

DMDirc is an IRC client written in Java. It's cross-platform, hugely
configurable, and is easily extensible with a robust plugins system.

This repository contains the 'core' of the client. If you're interested in
developing DMDirc or building it from scratch, you'd be much better off
cloning the [meta](https://github.com/DMDirc/meta) repository, which contains
the core, plugins, IRC library, etc. Detailed setup instructions are available
there as well.

Development information
--------------------------------------------------------------------------------

### Error handling

DMDirc has a user interface for displaying details of errors to users. It also
supports uploading of errors to the
[DMDirc sentry instance](https://sentry.dmdirc.com/) if the user allows it (or
manually clicks on send).

Errors should be logged using a [Slf4j](http://www.slf4j.org/) logger, and
marked with one markers defined in [LogUtils](https://github.com/DMDirc/DMDirc/blob/master/src/com/dmdirc/util/LogUtils.java).
These markers allow developers to specify whether an error is an "Application"
error (which will get reported and should ultimately be fixed), or a "User"
error (which is due to a problem with the user's config, environment, etc),
and also whether the error is fatal or not.

A typical class that reports errors will look something like the following:
```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.dmdirc.util.LogUtils.APP_ERROR;

public class MyClass {

    private static final Logger LOG = LoggerFactory.getLogger(MyClass.class);

    public void doSomething() {
        try {
            // Do something
        } catch (SomeException ex) {
            LOG.error(APP_ERROR, "Couldn't do something!", ex);
        }
    }

}
```
