package iut2.forbiddenisland.view.gui.game;

import iut2.forbiddenisland.controller.Controller;
import iut2.forbiddenisland.model.adventurer.Adventurer;

import javax.swing.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdventurerCardsPanel extends JPanel {

    private final Map<Adventurer, AdventurerCardPanel> panels = new HashMap<>(5);

    public AdventurerCardsPanel(final Controller controller, final int width, final int height) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        for (Adventurer adventurer : controller.getAdventurers().get()) {
            final AdventurerCardPanel panel = new AdventurerCardPanel(adventurer, width, height / 4);
            panels.put(adventurer, panel);
            add(panel);
        }
    }

    public Map<Adventurer, AdventurerCardPanel> getPanels() {
        return panels;
    }

    public void updateCards() {
        panels.values().forEach(AdventurerCardPanel::updateCards);
        invalidate();
    }

    public void setCurrentAdventurer(final Adventurer currentAdventurer) {
        panels.forEach((adv, panel) -> panel.setIsCurrentPlayer(adv == currentAdventurer));
    }

    public void setSendableAdventurers(final List<Adventurer> sendableAdventurers) {
        panels.forEach((adv, panel) -> panel.setSelectable(sendableAdventurers.contains(adv)));
    }
}
