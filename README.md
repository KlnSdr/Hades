# Hades

Hades is a simple and easy to use Identity and Access Management (IAM) system based on the
[dobby](https://github.com/klnsdr/dobby) webserver and [thot](https://github.com/klnsdr/thot)
object store.

It provides a simple-to-use API for managing users, groups and permissions. As well as a
simple web interface for managing users and groups.

The most relevant services available are:
- PermissionService for when you want to add permissions using code
- GroupService for when you want to create/assign groups on the fly
- UserService for when you need to work with user data
- The AutorizedRouteService and PermissionCheckService can be used to get all routes annotated with `@AuthorizedOnly`
  and `@PermissionCheck` respectively

Rest resources can be marked as `@AuthorizedOnly` and `@PermissionCheck` to enable access control. That way you can
be sure that only authorized users can access your resources and don't need to check for permissions in your
code manually.

The implemented update model ensures that a ready-to-use version of the system is available just by running the jar
for the first time.
To extend the
build in functionality with your own you can define your own updates by implementing the
`Update` interface and restarting the system. All new updates will be applied automatically.

As Hades is based on [dobby](https://github.com/klnsdr/dobby) version `^0.1.2` you can override the default style by
placing a `style.css` file in the static content directory of your project in a subfolder called `/hades` or
`/hades/login`.

## Configuration
- `hades.context`: The context path of the Hades web interface. Default: `/`
- `hades.login.maxAttempts`: The maximum number of login attempts before the user is locked out. Default: `5`
- `hades.login.lockDuration`: The duration in milliseconds a user is locked out after reaching the maximum number of login
  attempts. Default: `300000`
- `hades.login.redirect.success`: The URL to redirect to after a successful login. Default: `/`
- `hades.login.redirect.successAdmin`: The URL to redirect to after a successful login of an admin. Default: `/`