/**
 * <p>This software package is the graphical interface and command and control of the propinquity game. It is intended to be projected on the floor beneath the players. In addition controls all the customized hardware in the game. Finally it enforced where applicable game mechanics and scoring.</p>
 *
 * <p>Where possible the actual low level hardware code has been decoupled and placed in the {@link propinquity.hardware} package.</p>
 *
 * <p>The processing sketch file {@link propinquity.Propinquity} is the core of the program containing the draw loop and holding all the instantiated objects. Major components include the Level objects, Player objects. All level types subclass the abstract {@link propinquity.Level} object. Current implementations include {@link propinquity.ProxLevel} and the more rough {@link propinquity.BopperLevel} and {@link propinquity.HealthLevel}. The {@link propinquity.Player} represents a player, his score and it's visualisation. Setup and selections steps are done graphically using {@link propinquity.XBeeManager}, {@link propinquity.PlayerList}, {@link propinquity.PlayerSelect} and {@link propinquity.LevelSelect}.</p>
 *
 * <p>Propinquity is a full-body game that is a hybrid between fighting and dancing games. Two players wear proximity sensors on different body parts and as they move to the music, different sensors patches on their bodies light up to indicate when they are active. The players attempt to get as close as possible to active patches on the other player's body to score points. The longer s(he) can stay "in the sweet spot" (but without actually touching), the higher the resulting score.</p>
 *
 * <p><a href="http://playpr.hexagram.ca/projects/?proj=2">Propinquity on the TAG website</a></p>
 */

package propinquity;