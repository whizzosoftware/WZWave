## Overview

The goal of this project is to create a Java-native, open-source library for controlling Z-Wave
PC controllers.

This will allow you to control Z-Wave devices using a supported USB or serial controller and your
computer platform of choice.

![](https://raw.githubusercontent.com/whizzosoftware/WZWave/master/wzwave.jpg)

### Why WZWave?

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

