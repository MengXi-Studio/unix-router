# NavigationGuard

The route guard type. `beforeEach` / `beforeResolve` / `beforeEnter` and in-component guards all use this signature.

```ts
type NavigationGuard = (
	to: RouteLocation,
	from: RouteLocation
) => NavigationGuardReturn | Promise<NavigationGuardReturn>
```

## NavigationGuardReturn

```
void | boolean | string | RawLocationLike | NavigationRedirect | Error | null
```

| Returns | Behavior |
| --- | --- |
| `true` / `void` / `null` | Allow navigating |
| `false` | Abort the current navigation (`ABORTED`) |
| `string` / `{ path }` / `{ name }` | Redirect to the target location |
| `{ location, mode }` | Redirect, specifying the navigation mode (`push` / `replace` / `relaunch`) |
| `Error` | Abort the navigation and throw an error |

## Related Types

- [PostNavigationGuard](#postnavigationguard)
- [BeforeRouteEnterGuard / UpdateGuard / LeaveGuard](#in-component-guards)

### PostNavigationGuard

The after guard for `afterEach`, which receives an optional failure:

```ts
type PostNavigationGuard = (
	to: RouteLocation,
	from: RouteLocation,
	failure?: Error | null
) => void
```

### In-Component Guards

The guard types for `onBeforeRouteEnter` / `onBeforeRouteUpdate` / `onBeforeRouteLeave`, with signatures matching `NavigationGuard`.

## Related APIs

- [Route Guards Guide](../guide/guards)
- [Router - Guard Methods](./router-instance#guard-registration)