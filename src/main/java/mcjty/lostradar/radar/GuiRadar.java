package mcjty.lostradar.radar;

import com.mojang.blaze3d.systems.RenderSystem;
import mcjty.lib.client.RenderHelper;
import mcjty.lib.gui.*;
import mcjty.lib.gui.widgets.*;
import mcjty.lostradar.LostRadar;
import mcjty.lostradar.data.ClientMapData;
import mcjty.lostradar.data.MapPalette;
import mcjty.lostradar.data.PaletteCache;
import mcjty.lostradar.network.Messages;
import mcjty.lostradar.network.PacketStartSearch;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

import static mcjty.lib.gui.widgets.Widgets.positional;

public class GuiRadar extends GuiItemScreen implements IKeyReceiver {

    private static final int xSize = 340;
    private static final int ySize = 236;

    private static final ResourceLocation ICONS = new ResourceLocation(LostRadar.MODID, "textures/gui/icons.png");

    private WidgetList categoryList;
    private Button scanButton;

    public GuiRadar() {
        super(xSize, ySize, ManualEntry.EMPTY);
    }

    @Override
    public Window getWindow() {
        return window;
    }

    @Override
    public void init() {
        super.init();

        int k = (this.width - xSize) / 2;
        int l = (this.height - ySize) / 2;

        Panel toplevel = positional().filledRectThickness(2);
        categoryList = Widgets.list(238, 12, 93, ySize - 33);
        scanButton = Widgets.button(238, ySize - 28, 93, 15, "Scan")
                .event(() -> {
                    int selected = categoryList.getSelected();
                    if (selected >= 0) {
                        PaletteCache palette = PaletteCache.getOrCreatePaletteCache(MapPalette.getDefaultPalette(Minecraft.getInstance().level));
                        MapPalette.PaletteEntry entry = palette.getPalette().palette().get(selected);
                        String searchText = entry.name();
                        if (!searchText.isEmpty()) {
                            Messages.sendToServer(new PacketStartSearch(searchText));
                        }
                    }
                });
        toplevel.children(categoryList, scanButton);
        toplevel.bounds(k, l, xSize, ySize);
        populateCategoryList();

        window = new Window(this, toplevel);
    }

    private void renderMap(GuiGraphics graphics) {
        ClientMapData data = ClientMapData.getData();
        ChunkPos p = new ChunkPos(Minecraft.getInstance().player.blockPosition());
        // For an area of 21x21 chunks around the player we render the color
        int borderLeft = this.guiLeft + 12;
        int borderTop = this.guiTop + 12;
        int size = 10;
        int dim = 10;
        // Make a copy of searchResults so that we can modify it
        Set<ChunkPos> searchResults = new HashSet<>(data.getSearchResults());
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
                    int fullColor = 0xff000000 | (color & 0x00ffffff);
                    int startX = borderLeft + (x + dim) * size;
                    int startZ = borderTop + (z + dim) * size;
                    int borderColor = 0xff333333;
                    if (searchResults.contains(pos)) {
                        // This is a search result
                        borderColor = 0xffffffff;
                        searchResults.remove(pos);
                    }

                    if (entry == MapPalette.CITY) {
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
                        RenderHelper.drawBeveledBox(graphics, borderLeft + (x + dim) * size, borderTop + (z + dim) * size, borderLeft + (x + dim + 1) * size, borderTop + (z + dim + 1) * size, borderColor, borderColor, 0xff000000 + color);
                    }
                    if (entry.iconU() >= 0) {
                        // We have an icon
                        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                        graphics.blit(ICONS, startX+2, startZ+2, size-4, size-4, entry.iconU(), entry.iconV(), 32, 32, 256, 256);
                    }
                }
                if (x == 0 && z == 0) {
                    // Render the player as a white smaller dot
                    RenderHelper.drawBeveledBox(graphics, borderLeft + (x + dim) * size + 3, borderTop + (z + dim) * size + 3, borderLeft + (x + dim + 1) * size - 3, borderTop + (z + dim + 1) * size - 3, 0xffffffff, 0xffffffff, 0xffffffff);
                }
            }
        }
        // Now render the remaining search results but at the border (just beyond the map)
        for (ChunkPos pos : searchResults) {
            int dx = pos.x - p.x;
            int dz = pos.z - p.z;

            if (dx == 0 && dz == 0) continue; // Skip center

            int adx = Math.abs(dx);
            int adz = Math.abs(dz);
            int bx, bz;

            if (adx * dim >= adz * dim) {
                bx = Integer.signum(dx) * (dim + 1);
                bz = (int) Math.round((double) dz / adx * (dim + 1));
            } else {
                bz = Integer.signum(dz) * (dim + 1);
                bx = (int) Math.round((double) dx / adz * (dim + 1));
            }

            // Color based on distance to center:
            // - White when the search result is right at the center of the map
            // - Black when the search result is 80 chunks away
            // - Gray when the search result is in between
            int distance = Math.max(Math.abs(dx), Math.abs(dz));
            int minDistance = dim + 1;
            int maxDistance = 80;
            int clamped = Math.max(0, Math.min(255, (int)(255 * (1 - (double)(distance - minDistance) / (maxDistance - minDistance)))));
            int color = 0xff000000 | (clamped << 16) | (clamped << 8) | clamped;

//            double distance = Math.sqrt(dx * dx + dz * dz);
//            double minDistance = Math.sqrt(dim * dim + dim * dim);
//            double maxDistance = Math.sqrt(80 * 80 + 80 * 80);
//            int comp = (int) (255 * (1 - (distance - minDistance) / (maxDistance - minDistance)));
//            int color = 0xff000000 | (comp << 16) | (comp << 8) | comp;
            RenderHelper.drawBeveledBox(graphics, borderLeft + (bx + dim) * size + 3, borderTop + (bz + dim) * size + 3, borderLeft + (bx + dim + 1) * size - 3, borderTop + (bz + dim + 1) * size - 3, color, color, color);
        }
    }

    private void populateCategoryList() {
        int selected = categoryList.getSelected();
        categoryList.removeChildren();
        PaletteCache palette = PaletteCache.getOrCreatePaletteCache(MapPalette.getDefaultPalette(Minecraft.getInstance().level));
        for (MapPalette.PaletteEntry category : palette.getPalette().palette()) {
            categoryList.children(makeLine(category));
        }
        categoryList.selected(selected);
    }

    private Widget<Label>  makeLine(MapPalette.PaletteEntry category) {
        String name = category.name();
        if (name.isEmpty()) {
            name = "Unknown";
        }
        return Widgets.label(category.name());
    }

    @Override
    protected void renderInternal(@Nonnull GuiGraphics graphics, int xSize_lo, int ySize_lo, float par3) {
        boolean scanEnabled = categoryList.getSelected() >= 0;
        scanButton.enabled(scanEnabled);
        drawWindow(graphics);
        renderMap(graphics);
    }

    public static void open() {
        Minecraft.getInstance().setScreen(new GuiRadar());
    }

    @Override
    public void keyTypedFromEvent(int keyCode, int scanCode) {
        if (window != null) {
            if (window.keyTyped(keyCode, scanCode)) {
                super.keyPressed(keyCode, scanCode, 0); // @todo 1.14: modifiers?
            }
        }
    }

    @Override
    public void charTypedFromEvent(char codePoint) {
        if (window != null) {
            if (window.charTyped(codePoint)) {
                super.charTyped(codePoint, 0); // @todo 1.14: modifiers?
            }
        }
    }

    @Override
    public boolean mouseClickedFromEvent(double x, double y, int button) {
        WindowManager manager = getWindow().getWindowManager();
        manager.mouseClicked(x, y, button);
        return true;
    }

    @Override
    public boolean mouseReleasedFromEvent(double x, double y, int button) {
        WindowManager manager = getWindow().getWindowManager();
        manager.mouseReleased(x, y, button);
        return true;
    }

    @Override
    public boolean mouseScrolledFromEvent(double x, double y, double amount) {
        WindowManager manager = getWindow().getWindowManager();
        manager.mouseScrolled(x, y, amount);
        return true;
    }}
