package iut2.forbiddenisland.controller;

import iut2.forbiddenisland.controller.observer.NotifyOnSubscribeObservable;
import iut2.forbiddenisland.controller.observer.Observable;
import iut2.forbiddenisland.controller.request.Request;
import iut2.forbiddenisland.controller.request.RequestType;
import iut2.forbiddenisland.controller.request.Response;
import iut2.forbiddenisland.model.Board;
import iut2.forbiddenisland.model.Location;
import iut2.forbiddenisland.model.Treasure;
import iut2.forbiddenisland.model.WaterLevel;
import iut2.forbiddenisland.model.adventurer.Adventurer;
import iut2.forbiddenisland.model.card.*;
import iut2.forbiddenisland.model.cell.Cell;
import iut2.forbiddenisland.model.cell.CellState;
import iut2.forbiddenisland.model.cell.HeliportCell;
import iut2.forbiddenisland.model.cell.TreasureCell;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class GameEngine {

    private final PlayerManagement players;
    private final ModelProxy modelProxy;

    // Game data observables
    private final Observable<Map<Location, Cell>> cells;
    private final Observable<List<Adventurer>> adventurers;
    private final Observable<WaterLevel> waterLevel;
    private final Observable<List<Treasure>> treasures;
    private final Observable<Boolean> endGame;
    private final Observable<Integer> remainingActions;

    // Event related observables
    private final Observable<Void> risingWatersCardDrawn;
    private final Observable<Adventurer> tooManyCards;
    private final Observable<Adventurer> emergencyRescue;

    public GameEngine(final Board board, final List<Adventurer> players) {
        this.players = new PlayerManagement(players);

        modelProxy = new ModelProxy(board);

        cells = createCellsObs();
        adventurers = createAdventurersObs();
        waterLevel = createWaterLevelObs();
        treasures = createTreasureObs();
        endGame = new Observable<>();
        remainingActions = new NotifyOnSubscribeObservable<>(0);

        risingWatersCardDrawn = new Observable<>();
        tooManyCards = new Observable<>();
        emergencyRescue = new Observable<>();

        initGame();
        newPlayerRound();
    }

    // *** Factory methods for complex observables ***

    /**
     * Create an observable that will query the list of cells
     * at each update.
     *
     * @return An observable that auto updates it self when it changes.
     */
    private Observable<Map<Location, Cell>> createCellsObs() {
        return new NotifyOnSubscribeObservable<Map<Location, Cell>>() {
            @Override
            public void notifyChanges() {
                // Query all cells
                final Response<Map<Location, Cell>> allCells = modelProxy.request(
                        new Request(RequestType.CELLS_ALL, getCurrentPlayer().get())
                );

                value = allCells.getData();

                super.notifyChanges();
            }
        };
    }

    /**
     * Create an observable that will be notify each time an adventurer changes.
     *
     * @return An observable that will be notified when an adventurer changes.
     */
    private Observable<List<Adventurer>> createAdventurersObs() {
        return new NotifyOnSubscribeObservable<>(players.players);
    }

    /**
     * Create an observable that will keep an updated version of the water level.
     *
     * @return An observable for the current water level.
     */
    private Observable<WaterLevel> createWaterLevelObs() {
        return new NotifyOnSubscribeObservable<WaterLevel>() {
            @Override
            public void notifyChanges() {
                // Query water level
                final Response<WaterLevel> waterLevel = modelProxy.request(
                        new Request(RequestType.ISLAND_WATER_LEVEL, getCurrentPlayer().get())
                );

                value = waterLevel.getData();

                super.notifyChanges();
            }
        };
    }

    /**
     * Create an observable that will keep an updated version of the treasures.
     *
     * @return An observable of the treasures.
     */
    private Observable<List<Treasure>> createTreasureObs() {
        return new NotifyOnSubscribeObservable<List<Treasure>>() {
            @Override
            public void notifyChanges() {
                // Query treasures
                final Response<List<Treasure>> treasures = modelProxy.request(
                        new Request(RequestType.TREASURES_ALL, getCurrentPlayer().get())
                );

                value = treasures.getData();

                super.notifyChanges();
            }
        };
    }

    // *** Various getters used to expose some fields ***

    /**
     * Expose the current player.
     *
     * @return The current player's observable.
     */
    public Observable<Adventurer> getCurrentPlayer() {
        return players.current;
    }

    /**
     * Expose the updated number of actions left for the current player
     * this turn.
     *
     * @return The amount of actions left updated via an observable.
     */
    public Observable<Integer> getRemainingActions() {
        return remainingActions;
    }

    /**
     * Expose the cells of the board.
     * Updated when the state of a cell need a visual update.
     *
     * @return An observable of every cells currently on the board.
     */
    public Observable<Map<Location, Cell>> getCells() {
        return cells;
    }

    /**
     * Expose the adventurers of the board.
     * Updated when the state of an adventurer need a visual update.
     *
     * @return An observable of every adventurer of the board.
     */
    public Observable<List<Adventurer>> getAdventurers() {
        return adventurers;
    }

    /**
     * Expose the water level of the board.
     * Updated when the state of the water need a visual update.
     *
     * @return An observable of the water level of the board.
     */
    public Observable<WaterLevel> getWaterLevel() {
        return waterLevel;
    }

    /**
     * Expose the treasures of the board.
     * Updated when the state of the treasures need a visual update.
     *
     * @return An observable of each treasures of the board.
     */
    public Observable<List<Treasure>> getTreasures() {
        return treasures;
    }

    /**
     * Expose the status of the game
     *
     * @return An observable of the winning status of the game.
     */
    public Observable<Boolean> getEndGame() {
        return endGame;
    }

    // *** More getters that will trigger a request to the board ***

    /**
     * Get the observable that will be used to tell the user he just
     * got a rising waters card.
     *
     * @return When a rising water card is picked.
     */
    public Observable<Void> getRisingWatersCardDrawn() {
        return risingWatersCardDrawn;
    }

    /**
     * Get the observable that will be used to tell the user to discard a card.
     *
     * @return When a user need to get rid of a card.
     */
    public Observable<Adventurer> getTooManyCards() {
        return tooManyCards;
    }

    /**
     * Get the observable that will be used to notify that a player can be saved
     * in-extremis.
     *
     * @return When an emergency rescue is needed.
     */
    public Observable<Adventurer> getEmergencyRescue() {
        return emergencyRescue;
    }

    /**
     * Get all cells reachable by the player from the board.
     * The player can move to any of them successfully.
     *
     * @return The list of cells where the player is able to move.
     */
    public List<Cell> getReachableCells() {
        return getReachableCells(getCurrentPlayer().get());
    }

    public List<Cell> getReachableCells(final Adventurer target) {
        final Response<List<Cell>> reachableCells = modelProxy.request(
                new Request(RequestType.CELLS_REACHABLE, getCurrentPlayer().get())
                        .putData(Request.DATA_CELL, target.getPosition())
                        .putData(Request.DATA_PLAYER, target)
        );

        return reachableCells.getData();
    }

    /**
     * Get all cells dryable by the player from the board.
     *
     * @return The list of dryable cells.
     */
    public List<Cell> getDryableCells() {
        final Response<List<Cell>> dryableCells = modelProxy.request(
                new Request(RequestType.CELLS_DRAINABLE, getCurrentPlayer().get())
                        .putData(Request.DATA_CELL, getCurrentPlayer().get().getPosition())
        );

        return dryableCells.getData();
    }

    /**
     * Get all adventurers whose you can send a card to.
     *
     * @return The list of players you can reach.
     */
    public List<Adventurer> getPlayersSendable() {
        final Response<List<Adventurer>> sendablePlayers = modelProxy.request(
                new Request(RequestType.PLAYERS_SENDABLE, getCurrentPlayer().get())
        );

        return sendablePlayers.getData();
    }

    /**
     * Get all adventurers who are moveable by the
     * current player.
     *
     * @return The list of players moveable.
     */
    public List<Adventurer> getPlayersMoveable() {
        final Response<List<Adventurer>> moveablePlayers = modelProxy.request(
                new Request(RequestType.PLAYERS_MOVEABLE, getCurrentPlayer().get())
        );

        return moveablePlayers.getData();
    }

    // *** Player round related methods ***

    public void initGame() {
        for (Adventurer player : players.players) {
            // 2 cards for each players
            for (int i = 0; i < 2; ) {

                final Response<TreasureCard> res = modelProxy.request(
                        new Request(RequestType.CARD_DRAW, getCurrentPlayer().get())
                                .putData(Request.DATA_PLAYER, player)
                );

                // If rising waters card, throw it away
                if (res.getData() instanceof RisingWatersCard) {
                    trashCard(player, res.getData());
                } else {
                    ++i;
                }
            }
        }
    }

    /**
     * Start a new player round by settings up the needed variables of the engine.
     */
    public void newPlayerRound() {
        players.next();

        // Notify new round
        modelProxy.request(new Request(RequestType.GAME_NEW_ROUND, getCurrentPlayer().get()));

        // Query AP
        final Response<Integer> res = modelProxy.request(
                new Request(RequestType.GAME_MOVE_AMOUNT, getCurrentPlayer().get())
        );

        remainingActions.set(res.getData());

        // Notify new round
        endGame.set(null);
    }

    /**
     * Try to move the desired player to the desired cell.
     *
     * @param player - The player to move.
     * @param cell   - The cell to move the player to.
     * @return True if the action was successful, otherwise false.
     */
    public boolean movePlayer(final Adventurer player, final Cell cell) {
        final Response<Integer> res = modelProxy.request(
                new Request(RequestType.PLAYER_MOVE, getCurrentPlayer().get())
                        .putData(Request.DATA_PLAYER, player)
                        .putData(Request.DATA_CELL, cell)
        );

        if (res.isOk()) {
            decrementActions(res.getData());
        }

        return res.isOk();
    }

    /**
     * Try to move the desired player to the desired cell but in the
     * emergency mode.
     * From the point of view of the engine, the only difference with
     * {@link #movePlayer(Adventurer, Cell)} is that no actions will
     * be consumed for the current player.
     *
     * @param player - The player to move.
     * @param cell   - The cell to move the player to.
     * @return True if the action was successful, otherwise false.
     */
    public boolean emergencyMovePlayer(final Adventurer player, final Cell cell) {
        final Response<Integer> res = modelProxy.request(
                new Request(RequestType.PLAYER_MOVE, getCurrentPlayer().get())
                        .putData(Request.DATA_PLAYER, player)
                        .putData(Request.DATA_CELL, cell)
        );

        return res.isOk();
    }

    /**
     * Try to dry a cell.
     *
     * @param cell - The cell to dry
     */
    public boolean dryCell(final Cell cell) {
        final Response<Integer> res = modelProxy.request(
                new Request(RequestType.PLAYER_DRY, getCurrentPlayer().get())
                        .putData(Request.DATA_PLAYER, getCurrentPlayer().get())
                        .putData(Request.DATA_CELL, cell)
        );

        if (res.isOk()) {
            decrementActions(res.getData());
        }

        return res.isOk();
    }

    /**
     * Try to claim a treasure from a treasure cell.
     *
     * @param cell - The cell to claim.
     */
    public boolean claimTreasure(final TreasureCell cell) {
        final Response<Integer> res = modelProxy.request(
                new Request(RequestType.PLAYER_CLAIM, getCurrentPlayer().get())
                        .putData(Request.DATA_CELL, cell)
        );

        if (res.isOk()) {
            decrementActions(res.getData());
        }

        return res.isOk();
    }

    /**
     * Try to send a card to another adventurer.
     *
     * @param to   - The target adventurer.
     * @param card - The card to send.
     */
    public boolean sendCard(final Adventurer to, final Card card) {
        final Response<Integer> res = modelProxy.request(
                new Request(RequestType.PLAYER_SEND, getCurrentPlayer().get())
                        .putData(Request.DATA_PLAYER, getCurrentPlayer().get())
                        .putData(Request.DATA_PLAYER_EXTRA, to)
                        .putData(Request.DATA_CARD, card)
        );

        if (to.getCards().size() > 5) {
            tooManyCards.set(to);
        }

        if (res.isOk()) {
            decrementActions(res.getData());
        }

        return res.isOk();
    }

    /**
     * Use a card.
     *
     * @param source - The owner of the card.
     * @param card   - The card to use.
     */
    public boolean trashCard(final Adventurer source, final TreasureCard card) {
        final Response<Void> res = modelProxy.request(
                new Request(RequestType.CARD_TRASH, getCurrentPlayer().get())
                        .putData(Request.DATA_PLAYER, source)
                        .putData(Request.DATA_CARD, card)
        );

        return res.isOk();
    }

    /**
     * Use an helicopter card.
     * Will move every adventurer on the departure cell to the destination cell.
     *
     * @param source      - The owner of the card.
     * @param card        - The card.
     * @param departure   - The departure cell.
     * @param destination - The destination cell.
     * @return True if the operation succeeds, otherwise false.
     */
    public boolean useCardHelicopter(final Adventurer source, final HelicopterCard card,
                                     final Cell departure, final Cell destination) {

        if (departure.getAdventurers().isEmpty() || destination.getState() == CellState.FLOODED)
            return false;

        // Consume the card
        if (trashCard(source, card)) {

            // Make shallow copy of the adventurers to avoid concurrent modifications
            final List<Adventurer> adventurers = new ArrayList<>(departure.getAdventurers());

            // Directly alter the model (not a regular move operation)
            adventurers.forEach(adv -> adv.move(destination));

            return true;
        }

        return false;
    }

    /**
     * Use a sandbag card.
     * Will dry out the clicked cell for free.
     *
     * @param source - The owner of the card.
     * @param card   - The card.
     * @param toDry  - The cell to dry.
     * @return
     */
    public boolean useCardSandbag(final Adventurer source, final SandBagCard card, final Cell toDry) {

        if (toDry.getState() != CellState.WET)
            return false;

        // Consume the card
        if (trashCard(source, card)) {

            // Directly alter the cell (not a regular dry operation)
            toDry.setState(CellState.DRY);
            return true;
        }

        return false;
    }

    /**
     * End the 'manual' part of the current player's round.
     *
     * @return True if the sequence should continue, otherwise false.
     */
    public boolean endPlayerRound() {
        // Draw treasure cards
        final int drawAmount = modelProxy.<Integer>request(
                new Request(RequestType.CARD_DRAW_AMOUNT, getCurrentPlayer().get())
        ).getData();

        for (int i = 0; i < drawAmount; i++) {
            final Response<TreasureCard> res = modelProxy.request(
                    new Request(RequestType.CARD_DRAW, getCurrentPlayer().get())
                            .putData(Request.DATA_PLAYER, getCurrentPlayer().get())
            );

            if (res.getData() instanceof RisingWatersCard) {
                // Discard card
                trashCard(getCurrentPlayer().get(), res.getData());

                // In the real rules, if we draw 2 rising waters card, we shuffle only one time
                // but it doesn't really bother a computer to shuffle it 2 times

                // Water level +1 and reshuffle cards
                modelProxy.request(
                        new Request(RequestType.ISLAND_WATER_UP, getCurrentPlayer().get())
                                .putData(Request.DATA_AMOUNT, 1)
                );

                waterLevel.notifyChanges();
                risingWatersCardDrawn.notifyChanges();

                // If the water level is now at 10, game is lost
                if (waterLevel.get().getLevel() >= 10) {
                    endGame.set(false);
                    return false;
                }
            }

        }

        if (getCurrentPlayer().get().getCards().size() > 5) {
            tooManyCards.set(getCurrentPlayer().get());
        }

        return true;
    }

    private void decrementActions(final int amount) {
        remainingActions.set(remainingActions.get() - amount);
    }

    // *** Island turn related methods ***

    /**
     * Play the island turn by drawing flood cards.
     *
     * @return True if the sequence should continue, otherwise false.
     */
    public boolean startIslandTurn() {
        // Draw flood card
        final int drawAmount = modelProxy.<WaterLevel>request(
                new Request(RequestType.ISLAND_WATER_LEVEL, null)
        ).getData().computeAmountFloodCards();

        for (int i = 0; i < drawAmount; i++) {
            // Draw a card
            final Response<FloodCard> floodCardRes = modelProxy.request(new Request(RequestType.ISLAND_DRAW, null));

            // Use the card
            modelProxy.request(new Request(RequestType.ISLAND_APPLY, null).putData(Request.DATA_CARD, floodCardRes.getData()));

            final Cell cell = floodCardRes.getData().getTargetedCell();

            // If the cell is flooded
            if (cell.getState() == CellState.FLOODED) {

                // If a flooded treasure cell
                if (cell instanceof TreasureCell) {
                    final TreasureCell treasureCell = (TreasureCell) cell;

                    // If the treasure has already been claimed, we don't care
                    if (!treasureCell.getTreasure().isClaimable())
                        return true;

                    // Check for the sibling cell
                    final TreasureCell siblingCell = cells.get().values().stream()
                            .filter(c -> c instanceof TreasureCell)
                            // For convenience
                            .map(c -> (TreasureCell) c)
                            .filter(c -> c.getTreasure().equals(treasureCell.getTreasure()))
                            .filter(c -> !c.equals(treasureCell))
                            .findAny()
                            .get(); // The cell exist, no need to null-check it

                    // If the sibling cell is also flooded and the treasure isn't claimed, well, the game is lost
                    if (siblingCell.getState() == CellState.FLOODED) {
                        endGame.set(false);
                        return false;
                    }

                    // If the heliport is flooded
                } else if (cell instanceof HeliportCell) {
                    // Game is lost
                    endGame.set(false);
                    return false;
                }

                // If the cell is now flooded and there are adventurers on it
                if (!cell.getAdventurers().isEmpty()) {
                    // Make a shallow copy of the adventurers to rescue
                    final List<Adventurer> adventurers = new ArrayList<>(cell.getAdventurers());

                    // Check if the game is rescueable
                    for (Adventurer adventurer : adventurers) {
                        if (!isRescueable(adventurer)) {
                            endGame.set(false);
                            return false;
                        }
                    }

                    // Rescue each adventurers
                    adventurers.forEach(emergencyRescue::set);

                }
            }
        }

        return true;
    }

    private boolean isRescueable(final Adventurer adventurer) {

        // Get reachable cells for this adventurer
        final Response<List<Cell>> reachableRes = modelProxy.request(
                new Request(RequestType.CELLS_REACHABLE, adventurer)
                        .putData(Request.DATA_CELL, adventurer.getPosition())
        );

        // If there are no reachable cells, the game is lost
        return !reachableRes.getData().isEmpty();
    }

    /**
     * Manage the players of this game.
     * Manage who's gonna play next.
     */
    private class PlayerManagement {

        private final List<Adventurer> players;
        private final Observable<Adventurer> current;
        private Iterator<Adventurer> playersIterator;

        public PlayerManagement(final List<Adventurer> players) {
            this.players = players;
            current = new NotifyOnSubscribeObservable<>(null);
            reset();
        }

        public void reset() {
            playersIterator = players.iterator();
        }

        public void next() {
            if (!playersIterator.hasNext()) reset();
            current.set(playersIterator.next());
        }
    }

}
