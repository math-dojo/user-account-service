# Integration Tests

This executes a suite of tests from a node.js HTTP client against a local deployment of the
user-account-service function. This suite starts up the function before running the tests and
closes it and its associated processes after completing.

If the process does not close after the tests are complete, the `why-node-running` dependency
can be used to see why. For example:

```node
const log = require('why-is-node-running');

// Somewhere later, perhaps in finally
finally() {
    log();
}
```
