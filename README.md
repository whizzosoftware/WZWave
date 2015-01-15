[![Build Status](https://travis-ci.org/whizzosoftware/WZWave.svg)](https://travis-ci.org/whizzosoftware/WZWave)
[![Coverage Status](https://coveralls.io/repos/whizzosoftware/WZWave/badge.svg)](https://coveralls.io/r/whizzosoftware/WZWave)

## Overview

The goal of this project is to create a Java-native, open-source library for controlling Z-Wave
PC controllers.

Z-Wave is a wireless protocol used for home automation. It use low-power RF to control smart devices such as
lights, power outlets, thermostats and more. A PC controller (e.g. a USB dongle) provides applications a gateway
to the wireless Z-Wave device network. More information can be found at
[this Wikipedia article](http://en.wikipedia.org/wiki/Z-Wave).

![](https://raw.githubusercontent.com/whizzosoftware/WZWave/master/wzwave.jpg)

### Why Z-Wave?

The Z-Wave alliance has over 250 independent manufacturers as of 2014 (source: Wikipedia) including big names like
GE, ADT, Ingersoll-Rand and Trane. The smart devices are relatively inexpensive, readily available and interoperable
between manufacturers (something that can't be said for some other wireless home automation technologies out there).

### Why WZWave?

The biggest drawback with Z-Wave is the PC controller serial protocol is not freely available. To obtain official
information requires signing an NDA and paying a hefty fee to get the Z-Wave SDK. As I understand it, software
developed under the NDA cannot be made freely available. Hence the need for projects like this.

In my research, I was unable to find any native Java libraries that would interface with a Z-Wave controller. Granted,
there were libraries that used JNI to wrapper the excellent
[open-zwave library](https://code.google.com/p/open-zwave/) but that meant deferring most of the work to a binary
library. Thus, WZWave was created to be a fully Java-native solution.

WZWave was created using a combination of studying online resources, studying the open-zwave project as well as
copious amounts of reverse engineering.

Note: The project is not affiliated or endorsed by Sigma Designs and none of their IP was used in the creation of this
library.

### Status

WZWave is still very early days and is under active development. Only a limited subset of devices have been tested
but support will continue to grow over time.

I encourage anyone that wants to get involved to please do so. The project really needs developers to get involved
to help increase robustness and device support!

Please see the project's [wiki page](https://whizzosoftware.atlassian.net/wiki/display/WZWAV/WZWave+Home) for more
information.

