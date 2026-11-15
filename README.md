# Backhaul

Share your Android device's Internet connection with other devices without needing Root.

## Get Backhaul

Official releases are published on GitHub only. Get the APK from the
[Releases Section](https://github.com/DocBobert6/Backhaul/releases/latest).

## Screenshots

### Hotspot Status

#### Status Overview

[<img
src="https://raw.githubusercontent.com/pyamsoft/tetherfi/main/art/screens/phone/main-light.png"
alt="Light Mode: Hotspot Status"
height="200">](https://raw.githubusercontent.com/pyamsoft/tetherfi/main/art/screens/phone/main-light.png)
[<img
src="https://raw.githubusercontent.com/pyamsoft/tetherfi/main/art/screens/phone/main-dark.png"
alt="Dark Mode: Hotspot Status"
height="200">](https://raw.githubusercontent.com/pyamsoft/tetherfi/main/art/screens/phone/main-dark.png)

### Hotspot Behavior

#### Operation Settings

[<img
src="https://raw.githubusercontent.com/pyamsoft/tetherfi/main/art/screens/phone/behavior-light.png"
alt="Light Mode: Operating Settings"
height="200">](https://raw.githubusercontent.com/pyamsoft/tetherfi/main/art/screens/phone/behavior-light.png)
[<img
src="https://raw.githubusercontent.com/pyamsoft/tetherfi/main/art/screens/phone/behavior-dark.png"
alt="Dark Mode: Operating Settings"
height="200">](https://raw.githubusercontent.com/pyamsoft/tetherfi/main/art/screens/phone/behavior-dark.png)

### Hotspot Active

#### In-App

[<img
src="https://raw.githubusercontent.com/pyamsoft/tetherfi/main/art/screens/phone/running-light.png"
alt="Light Mode: Hotspot On"
height="200">](https://raw.githubusercontent.com/pyamsoft/tetherfi/main/art/screens/phone/running-light.png)
[<img
src="https://raw.githubusercontent.com/pyamsoft/tetherfi/main/art/screens/phone/running-dark.png"
alt="Dark Mode: Hotspot On"
height="200">](https://raw.githubusercontent.com/pyamsoft/tetherfi/main/art/screens/phone/running-dark.png)

## What is TetherFuseNet

TetherFuseNet works by creating a Wi-Fi Direct legacy group and an HTTP proxy server. Other
devices can connect to the broadcasted Wi-Fi network, and connect to the Internet by
setting the proxy server settings to the server created by TetherFuseNet. You do not need a
Hotspot data plan to use TetherFuseNet, but the app works best with "unlimited" data plans.

### TetherFuseNet may be for you if:

- You want to share your Android's Wi-Fi or Cellular Data
- You have an Unlimited Data and a Hotspot plan from your Carrier, but Hotspot
  has a data cap
- You have an Unlimited Data and a Hotspot plan from your Carrier, but Hotspot
  has throttling
- You do not have a mobile Hotspot plan
- You wish to create a LAN between devices
- Your home router has reached the device connection limit

## How

TetherFuseNet uses a Foreground Service to create a long-running Wi-Fi Direct Network that
other devices can connect to. Connected devices can exchange network data between each other.
The user is in full control of this Foreground Service and can explicitly choose when to
turn it on and off.

TetherFuseNet is still a work in progress and not everything will work. For example, using the
app to get an open NAT type on consoles is currently not possible. Using TetherFuseNet for certain
online apps, chat apps, video apps, and gaming apps is currently not possible. Some services
such as email may be unavailable. General "normal" internet browsing should work fine - however,
it is dependent on the speed and availability of your Android device's internet connection.

To see a list of apps that are known to not work currently, see the
[Wiki](https://github.com/pyamsoft/tetherfusenet/wiki/Known-Not-Working)

## Privacy

TetherFuseNet respects your privacy. TetherFuseNet is open source, and always will be. TetherFuseNet
will never track you, or sell or share your data. TetherFuseNet offers in-app purchases,
which you may purchase to support the developer. These purchases are never
required to use the application or any features.

## Development

TetherFuseNet is developed in the open on GitHub at:

[https://github.com/DocBobert6/Backhaul](https://github.com/DocBobert6/Backhaul)

If you know a few things about Android programming and want to help out with
development, you can do so by creating issue tickets to squash bugs, and
proposing feature requests.

## Wiki

The [Wiki](https://github.com/pyamsoft/tetherfusenet/wiki) is hosted on Github, but a versioned copy
exists in this repository in the `wiki/` folder. The `wiki/` is a submodule and can be updated via
`git submodule update --init --force`

## License

Apache 2

```
Copyright 2026 pyamsoft

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
