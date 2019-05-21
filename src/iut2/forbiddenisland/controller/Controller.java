package iut2.forbiddenisland.controller;

import iut2.forbiddenisland.controller.observer.NotifyOnSubscribeObservable;
import iut2.forbiddenisland.controller.observer.Observable;
import iut2.forbiddenisland.model.*;

import java.util.List;

public class Controller {

    private final GameEngine engine;
    private final Observable<GameMode> gameMode;
    private Adventurer selectedAdventurer;
    private Card selectedCard;

    public Controller(final Board board, final Adventurer... adventurers) {
        engine = new GameEngine(board, adventurers);
        gameMode = new NotifyOnSubscribeObservable<>(GameMode.IDLE);
    }

    public Observable<GameMode> getGameMode() {
        return gameMode;
    }

    public Observable<List<Cell>> getCells() {
        return engine.getCells();
    }

    public Observable<Integer> getRemainingActions() {
        return engine.getRemainingActions();
    }

    public Observable<List<Adventurer>> getAdventurers() {
        return engine.getAdventurers();
    }

    /**
     * The controller will subscribe to the provided observable
     * and enable the move mode of the engine when triggered.
     *
     * @param o - The observable to observe.
     */
    public void observeModeMove(final Observable<Void> o) {
        o.subscribe(value -> gameMode.set(GameMode.MOVE));
    }

    /**
     * The controller will subscribe to the provided observable
     * and enable the dry mode of the engine when triggered.
     *
     * @param o - The observable to observe.
     */
    public void observeModeDry(final Observable<Void> o) {
        o.subscribe(value -> gameMode.set(GameMode.DRY));
    }

    /**
     * The controller will subscribe to the provided observable
     * and enable the treasure claiming mode of the engine when triggered.
     *
     * @param o - The observable to observe.
     */
    public void observeModeTreasureClaim(final Observable<Void> o) {
        o.subscribe(value -> gameMode.set(GameMode.TREASURE));
    }

    /**
     * The controller will subscribe to the provided observable
     * and enable the sending mode of the engine when triggered.
     *
     * @param o - The observable to observe.
     */
    public void observeModeSend(final Observable<Void> o) {
        o.subscribe(value -> gameMode.set(GameMode.SEND));
    }

    /**
     * The controller will subscribe to the provided observable
     * and communicate the click of a cell to the engine when triggered.
     *
     * @param o - The observable to observe.
     */
    public void observeClickCell(final Observable<Cell> o) {
        o.subscribe(cell -> {
            switch (gameMode.get()) {
                case MOVE:
                    engine.movePlayer(cell, selectedAdventurer);
                    break;
                case DRY:
                    engine.dryCell(cell);
                    break;
                case TREASURE:
                    if (cell instanceof TreasureCell)
                        engine.claimTreasure((TreasureCell) cell);
                    break;
                default:
                    break;
            }
        });
    }

    /**
     * The controller will subscribe to the provided observable
     * and communicate the click of a player to the engine when triggered.
     *
     * @param o - The observable to observe.
     */
    public void observeClickPlayer(final Observable<Adventurer> o) {
        o.subscribe(adventurer -> {
            switch (gameMode.get()) {
                case SEND:
                    selectedAdventurer = adventurer;

                    if (selectedCard != null)
                        engine.sendCard(selectedAdventurer, selectedCard);
                    break;
                case MOVE:
                    selectedAdventurer = adventurer;
                    break;
                default:
                    break;
            }
        });
    }

    /**
     * The controller will subscribe to the provided observable
     * and communicate the click of a card to the engine when triggered.
     *
     * @param o - The observable to observe.
     */
    public void observeClickCard(final Observable<Card> o) {
        o.subscribe(card -> {
            switch (gameMode.get()) {
                case SEND:
                    selectedCard = card;

                    if (selectedAdventurer != null)
                        engine.sendCard(selectedAdventurer, selectedCard);
                    break;
                default:
                    engine.useCard(card);
                    break;
            }
        });
    }

}
