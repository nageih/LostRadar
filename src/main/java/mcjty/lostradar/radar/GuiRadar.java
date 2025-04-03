package mcjty.lostradar.radar;

import mcjty.lib.gui.GuiItemScreen;
import mcjty.lib.gui.ManualEntry;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lostradar.setup.Messages;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import javax.annotation.Nonnull;

import static mcjty.lib.gui.layout.AbstractLayout.DEFAULT_VERTICAL_MARGIN;
import static mcjty.lib.gui.widgets.Widgets.vertical;

public class GuiRadar extends GuiItemScreen {

    private static final int xSize = 340;
    private static final int ySize = 220;

    public GuiRadar() {
        super(xSize, ySize, ManualEntry.EMPTY);
    }

    @Override
    public void init() {
        super.init();

        int k = (this.width - xSize) / 2;
        int l = (this.height - ySize) / 2;

        Panel toplevel = vertical(DEFAULT_VERTICAL_MARGIN, 0).filledRectThickness(2);

        // setup
        toplevel.bounds(k, l, xSize, ySize);

        window = new Window(this, toplevel);
        Messages.sendToServer(new PacketRequestMap(Minecraft.getInstance().player.blockPosition()));
    }

    @Override
    protected void renderInternal(@Nonnull GuiGraphics graphics, int xSize_lo, int ySize_lo, float par3) {
        drawWindow(graphics);
    }

    public static void open() {
        Minecraft.getInstance().setScreen(new GuiRadar());
    }

}
