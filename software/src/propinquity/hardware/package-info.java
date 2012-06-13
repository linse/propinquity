/**
 * <p>The low level hardware code and hardware abstractions have been decoupled and placed in this package. The intent is that many different hardware interface could be provided.</p>
 *
 * <p>Such hardware interface would implement the {@link propinquity.hardware.HardwareInterface} interface. For the moment there is the actual XBee implementation given in {@link propinquity.hardware.XBeeBaseStation} and the simulator which allows testing without XBees or actual hardware given in {@link propinquity.hardware.HardwareSimulator}. All data coming from hardware is passed back to registered listener objects which implement the {@link propinquity.hardware.ProxEventListener} interface.</p>
 *
 * <p>The package also contains the object which represent the actual hardware. Currently these are {@link propinquity.hardware.Glove} and {@link propinquity.hardware.Patch}.</p>
 *
 * <p>The {@link propinquity.hardware.Packet} object and the {@link propinquity.hardware.PacketType} enum, specify together the current packet format used for communication.</p>
 *
 * <p>Finally some rough debugging code is provided. In particular the {@link propinquity.hardware.HardwareDebugger} provided a rough interface to test hardware without running the full application.</p>
 */
package propinquity.hardware;