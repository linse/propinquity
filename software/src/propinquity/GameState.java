package propinquity;

/**
 * Encodes the current state of the game.
 *
 */
public enum GameState {
	/** Scanning for Xbees. */
	XBeeInit, 
	/** Input player names. */
	PlayerList,
	/** Select player colors. */
	PlayerSelect, 
	/** Select the level. */
	LevelSelect, 
	/** Play a level. */
	Play;
}
