package iut2.forbiddenisland.controller;

public enum RequestType {
	// Player related requests
	PLAYERS_ALL,
	PLAYER_MOVE_AMOUNT,
	PLAYERS_SENDABLE,
	PLAYER_MOVE,
	PLAYER_SEND,
	PLAYER_USE_CARD,

	// Cells related requests,
	CELLS_ALL,
	CELLS_REACHABLE,
	CELLS_DRYABLE,
	CELL_DRY,
	CELL_CLAIM_TREASURE,

	// Game related requests
	TREASURES_CLAIMABLE,
	FLOODING
}
