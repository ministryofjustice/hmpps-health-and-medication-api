[< Back](./building_and_running.md)
---

## sdkman

It is recommended to use [sdkman](https://sdkman.io/) to ensure that you are running the same version of Java that is 
used in CI and in production.

To do this on a mac:

```shell
curl -s "https://get.sdkman.io" | bash
```

Close the terminal and reopen. Then run **within the repository folder**:

```shell
sdk env
```

This will pick up the `.sdkmanrc` file which contains the java version that should be used.

To enable sdkman to automatically switch you to the correct java version when changing directory in the terminal,
edit `~/.sdkman/etc/config` and set `sdkman_auto_env=true`
