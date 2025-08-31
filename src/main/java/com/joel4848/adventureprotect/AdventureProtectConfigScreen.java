package com.joel4848.adventureprotect;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class AdventureProtectConfigScreen extends Screen {
    private final Screen parent;
    private final AdventureProtectConfig config = AdventureProtectConfig.INSTANCE;

    public AdventureProtectConfigScreen(Screen parent) {
        super(Text.literal("AdventureProtect Config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        GridWidget gridWidget = new GridWidget();
        gridWidget.getMainPositioner().marginX(5).marginBottom(4).alignHorizontalCenter();
        GridWidget.Adder adder = gridWidget.createAdder(2);

        // Title
        adder.add(ButtonWidget.builder(Text.literal("AdventureProtect Configuration").formatted(Formatting.BOLD), button -> {})
                .width(200).size(200, 20).build(), 2);

        // Block interactions
        adder.add(createToggleButton("Disable Trapdoor Interaction", config.DisableTrapdoorInteraction,
                (value) -> config.DisableTrapdoorInteraction = value));
        adder.add(createToggleButton("Disable Flowerpot Interaction", config.DisableFlowerpotInteraction,
                (value) -> config.DisableFlowerpotInteraction = value));

        adder.add(createToggleButton("Disable Chest Interaction", config.DisableChestInteraction,
                (value) -> config.DisableChestInteraction = value));
        adder.add(createToggleButton("Disable Barrel Interaction", config.DisableBarrelInteraction,
                (value) -> config.DisableBarrelInteraction = value));

        adder.add(createToggleButton("Disable Brewing Stand Interaction", config.DisableBrewingStandInteraction,
                (value) -> config.DisableBrewingStandInteraction = value));
        adder.add(createToggleButton("Disable Note Block Interaction", config.DisableNoteBlockInteraction,
                (value) -> config.DisableNoteBlockInteraction = value));

        adder.add(createToggleButton("Disable Jukebox Interaction", config.DisableJukeboxInteraction,
                (value) -> config.DisableJukeboxInteraction = value));
        adder.add(createToggleButton("Disable Decorated Pot Interaction", config.DisableDecoratedPotInteraction,
                (value) -> config.DisableDecoratedPotInteraction = value));

        adder.add(createToggleButton("Disable Shulker Box Interaction", config.DisableShulkerBoxInteraction,
                (value) -> config.DisableShulkerBoxInteraction = value));
        adder.add(createToggleButton("Disable XercaMusic Interaction", config.DisableXercaMusicInteraction,
                (value) -> config.DisableXercaMusicInteraction = value));

        // Entity interactions
        adder.add(createToggleButton("Disable Item Frame Interaction", config.DisableItemFrameInteraction,
                (value) -> config.DisableItemFrameInteraction = value));
        adder.add(createToggleButton("Disable Painting Interaction", config.DisablePaintingInteraction,
                (value) -> config.DisablePaintingInteraction = value));

        // Armor stand options
        adder.add(createToggleButton("Disable Armour Stand Remove Items", config.DisableArmourStandRemoveItems,
                (value) -> config.DisableArmourStandRemoveItems = value));
        adder.add(createToggleButton("Disable Armour Stand Place Items", config.DisableArmourStandPlaceItems,
                (value) -> config.DisableArmourStandPlaceItems = value));

        adder.add(createToggleButton("Disable Armour Stand Replace Items", config.DisableArmourStandReplaceItems,
                (value) -> config.DisableArmourStandReplaceItems = value));
        adder.add(createToggleButton("Disable Easel Interaction", config.DisableEaselInteraction,
                (value) -> config.DisableEaselInteraction = value));

        adder.add(createToggleButton("Disable Placed Canvas Interaction", config.DisablePlacedCanvasInteraction,
                (value) -> config.DisablePlacedCanvasInteraction = value));
        adder.add(createToggleButton("Disable Placed Photograph Interaction", config.DisablePlacedPhotographInteraction,
                (value) -> config.DisablePlacedPhotographInteraction = value));

        // Done and Cancel buttons
        adder.add(ButtonWidget.builder(ScreenTexts.DONE, button -> {
            AdventureProtectConfig.save();
            if (this.client != null) {
                this.client.setScreen(this.parent);
            }
        }).size(100, 20).build());

        adder.add(ButtonWidget.builder(ScreenTexts.CANCEL, button -> {
            AdventureProtectConfig.load(); // Reload from file to discard changes
            if (this.client != null) {
                this.client.setScreen(this.parent);
            }
        }).size(100, 20).build());

        gridWidget.refreshPositions();
        SimplePositioningWidget.setPos(gridWidget, 0, 0, this.width, this.height, 0.5f, 0.25f);
        gridWidget.forEachChild(this::addDrawableChild);
    }

    private ButtonWidget createToggleButton(String text, boolean currentValue, ToggleCallback callback) {
        Text buttonText = Text.literal(text + ": " + (currentValue ? "ON" : "OFF"))
                .formatted(currentValue ? Formatting.GREEN : Formatting.RED);

        return ButtonWidget.builder(buttonText, button -> {
            boolean newValue = !currentValue;
            callback.toggle(newValue);

            Text newButtonText = Text.literal(text + ": " + (newValue ? "ON" : "OFF"))
                    .formatted(newValue ? Formatting.GREEN : Formatting.RED);
            button.setMessage(newButtonText);
        }).size(180, 20).build();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        // Draw title
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 16777215);

        // Draw description
        Text description = Text.literal("Configure which interactions are disabled for Adventure Mode players");
        context.drawCenteredTextWithShadow(this.textRenderer, description, this.width / 2, 30, 10526880);
    }

    @FunctionalInterface
    private interface ToggleCallback {
        void toggle(boolean value);
    }
}