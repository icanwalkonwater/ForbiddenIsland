package iut2.forbiddenisland.model.card;

import iut2.forbiddenisland.model.cell.Cell;

/**
 * Represent a card from the flood deck. Only playable by the island.
 */
public class FloodCard extends Card {

    private final Cell cell;

    public FloodCard(final Cell cell) {
        super(cell.getName());
        this.cell = cell;
    }

    /**
     * Get the cell targeted by this flood card.
     *
     * @return The targeted cell.
     */
    public Cell getTargetedCell() {
        return cell;
    }
}
