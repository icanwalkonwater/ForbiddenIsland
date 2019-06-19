package iut2.forbiddenisland.view.gui.game;

import iut2.forbiddenisland.controller.Controller;
import iut2.forbiddenisland.controller.observer.Observable;
import iut2.forbiddenisland.model.Location;
import iut2.forbiddenisland.model.Treasure;
import iut2.forbiddenisland.model.cell.Cell;
import iut2.forbiddenisland.view.gui.utils.GridCellButton;
import iut2.forbiddenisland.view.gui.utils.TreasureImage;

import javax.swing.*;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BoardPanel extends JPanel {

    private final Observable<Cell> cellClickNotifier = new Observable<>();

    private final List<TreasureImage> treasurePanels = new ArrayList<>(4);
    private final Map<Location, GridCellButton> cellsPanels = new HashMap<>();

    public BoardPanel(final Controller controller) {
        setLayout(new GridLayout(8, 8));

        controller.observeClickCell(cellClickNotifier);

        setup(controller.getCells().get(), controller.getTreasures().get());
        update();
    }

    private void setup(final Map<Location, Cell> cells, final List<Treasure> treasures) {
        if (treasures.size() != 4)
            throw new IllegalArgumentException("There need to be exactly 4 treasures !");

        // Line by line

        // *** Line 0 ***

        for (int i = 0; i < 8; ++i)
            add(createEmpty());

        // *** Line 1 ***

        add(createEmpty());

        // First treasure
        add(createTreasureDisplay(treasures.get(0)));

        add(createEmpty());

        add(createCellButton(cells.get(Location.from(2, 0))));
        add(createCellButton(cells.get(Location.from(3, 0))));

        add(createEmpty());

        // Second treasure
        add(createTreasureDisplay(treasures.get(1)));

        add(createEmpty());

        // *** Line 2 ***

        add(createEmpty());
        add(createEmpty());

        for (int i = 1; i <= 4; ++i)
            add(createCellButton(cells.get(Location.from(i, 1))));

        add(createEmpty());
        add(createEmpty());

        // *** Line 3 ***

        add(createEmpty());

        for (int i = 0; i <= 5; ++i)
            add(createCellButton(cells.get(Location.from(i, 2))));

        add(createEmpty());

        // *** Line 4 ***

        add(createEmpty());

        for (int i = 0; i <= 5; ++i)
            add(createCellButton(cells.get(Location.from(i, 3))));

        add(createEmpty());

        // *** Line 5 ***

        add(createEmpty());
        add(createEmpty());

        for (int i = 1; i <= 4; ++i)
            add(createCellButton(cells.get(Location.from(i, 4))));

        add(createEmpty());
        add(createEmpty());

        // *** Line 6 ***

        add(createEmpty());

        // Third treasure
        add(createTreasureDisplay(treasures.get(2)));

        add(createEmpty());

        add(createCellButton(cells.get(Location.from(2, 5))));
        add(createCellButton(cells.get(Location.from(3, 5))));

        add(createEmpty());

        // Fourth treasure
        add(createTreasureDisplay(treasures.get(3)));

        add(createEmpty());

        // *** Line 7 ***

        for (int i = 0; i < 8; ++i)
            add(createEmpty());
    }

    private void update() {
        treasurePanels.forEach(TreasureImage::update);
        cellsPanels.values().forEach(GridCellButton::update);
        repaint();
    }

    private JPanel createEmpty() {
        return new JPanel();
    }

    private GridCellButton createCellButton(final Cell cell) {
        final GridCellButton btn = new GridCellButton(cell);
        btn.addActionListener(e -> cellClickNotifier.set(cell));

        cellsPanels.put(cell.getLocation(), btn);
        return btn;
    }

    private TreasureImage createTreasureDisplay(final Treasure treasure) {
        final TreasureImage display = new TreasureImage(treasure);
        treasurePanels.add(display);

        return display;
    }
}
