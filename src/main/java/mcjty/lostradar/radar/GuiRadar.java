package mcjty.lostradar.radar;

import com.mojang.blaze3d.systems.RenderSystem;
import mcjty.lib.client.RenderHelper;
import mcjty.lib.gui.GuiItemScreen;
import mcjty.lib.gui.ManualEntry;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lostradar.LostRadar;
import mcjty.lostradar.data.*;
import mcjty.lostradar.network.PacketRequestMapChunk;
import mcjty.lostradar.network.Messages;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;

import javax.annotation.Nonnull;

import static mcjty.lib.gui.layout.AbstractLayout.DEFAULT_VERTICAL_MARGIN;
import static mcjty.lib.gui.widgets.Widgets.vertical;

public class GuiRadar extends GuiItemScreen {

    private static final int xSize = 340;
    private static final int ySize = 236;

    private static final ResourceLocation ICONS = new ResourceLocation(LostRadar.MODID, "textures/gui/icons.png");

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
    }

    private void renderMap(GuiGraphics graphics) {
        ClientMapData data = ClientMapData.getData();
        ChunkPos p = new ChunkPos(Minecraft.getInstance().player.blockPosition());
        // For an area of 21x21 chunks around the player we render the color
        int borderLeft = this.guiLeft + 16;
        int borderTop = this.guiTop + 12;
        int size = 10;
        int dim = 10;
        for (int x = -dim; x <= dim; x++) {
            for (int z = -dim; z <= dim; z++) {
                ChunkPos pos = new ChunkPos(p.x + x, p.z + z);
                int biomeColor = data.getBiomeColor(Minecraft.getInstance().level, pos);
                if (biomeColor != -1) {
                    // Render the biome color
                    RenderHelper.drawBeveledBox(graphics, borderLeft + (x+dim) * size, borderTop + (z+dim) * size, borderLeft + (x + dim + 1) * size, borderTop + (z + dim + 1) * size, 0xff000000 + biomeColor, 0xff000000 + biomeColor, 0xff000000 + biomeColor);
                }
                MapPalette.PaletteEntry entry = data.getPaletteEntry(Minecraft.getInstance().level, pos);
                if (entry != null) {
                    // Render the color
                    int color = entry.color();
                    int startX = borderLeft + (x + dim) * size;
                    int startZ = borderTop + (z + dim) * size;

                    if (entry == MapPalette.CITY) {
                        int fullColor = 0xff000000 | (color & 0x00ffffff);
                        RenderHelper.drawBeveledBox(graphics, startX, startZ, startX + size, startZ + size, fullColor, fullColor, fullColor);

                        // Determine pattern offset
                        int patternOffsetX = x % 2;
                        int patternOffsetZ = z % 2;

                        // Draw 5×5 dithered black squares
                        int step = size / 5;
                        for (int i = 0; i < 5; i++) {
                            for (int j = 0; j < 5; j++) {
                                // Simple dither condition — you can tweak this for different patterns
                                if ((i + j + patternOffsetX + patternOffsetZ) % 2 == 0) {
                                    int x0 = startX + i * step;
                                    int z0 = startZ + j * step;
                                    int x1 = startX + (i + 1) * step;
                                    int z1 = startZ + (j + 1) * step;
                                    RenderHelper.drawBeveledBox(graphics, x0, z0, x1, z1, 0xff000000, 0xff000000, 0xff000000);
                                }
                            }
                        }
                    } else {
                        RenderHelper.drawBeveledBox(graphics, borderLeft + (x + dim) * size, borderTop + (z + dim) * size, borderLeft + (x + dim + 1) * size, borderTop + (z + dim + 1) * size, 0xff333333, 0xff333333, 0xff000000 + color);
                    }
                    if (entry.iconU() >= 0) {
                        // We have an icon
                        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                        graphics.blit(ICONS, startX+2, startZ+2, size-4, size-4, entry.iconU(), entry.iconV(), 32, 32, 256, 256);
//                        graphics.blit(ICONS, startX, startZ, size, size, 0, 96, 32, 23, 256, 256);
                    }
                }
                if (x == 0 && z == 0) {
                    // Render the player as a white smaller dot
                    RenderHelper.drawBeveledBox(graphics, borderLeft + (x + dim) * size + 3, borderTop + (z + dim) * size + 3, borderLeft + (x + dim + 1) * size - 3, borderTop + (z + dim + 1) * size - 3, 0xffffffff, 0xffffffff, 0xffffffff);
                }
            }
        }
    }

    @Override
    protected void renderInternal(@Nonnull GuiGraphics graphics, int xSize_lo, int ySize_lo, float par3) {
        drawWindow(graphics);
        renderMap(graphics);
    }

    public static void open() {
        Minecraft.getInstance().setScreen(new GuiRadar());
    }

}
