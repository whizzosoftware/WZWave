WZWave
======

Overview
========

The goal of this project is to create a native, open-source Z-Wave library for Java. 
It was created using a combination of studying online resources, studying the `open-zwave`_
project as well as reverse engineering.

The primary use case for this project is to support the Dominion home automation server.
The home page for that project can be found here:

    http://www.dominion-automation.com

However, it is being provided to the community in the hope that it will be used
elsewhere and contributed to.

The library has been tested only with the `Aeotec Z-Stick`_ and supports a limited number
of Z-Wave devices. Device support will continue to grow over time.

WZwave uses the RXTX library to access the serial port, so you will need to have the appropriate
native library for the platform you are running it on.

Mac RXTX Installation
=====================

1. Install the latest `Silicon Labs VCP driver`_ driver from here.

2. Make sure you have a ``librxtxSerial.jnilib`` in your ``/Library/Java/Extensions`` folder. If not, you can download the `Mac librxtx JNI library`_.

3. Run:

    $ sudo mkdir /var/lock

4. Run:

    $ sudo chmod a+rw /var/lock

.. _`open-zwave`: https://code.google.com/p/open-zwave/
.. _`Aeotec Z-Stick`: http://aeotec.com/z-wave-usb-stick
.. _`Silicon Labs VCP driver`: http://www.silabs.com/products/mcu/Pages/USBtoUARTBridgeVCPDrivers.aspx
.. _`Mac librxtx JNI library`: http://www.dominion-automation.com/downloads/librxtxSerial.jnilib.zip