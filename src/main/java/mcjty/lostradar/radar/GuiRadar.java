package mcjty.lostradar.radar;

import com.mojang.blaze3d.systems.RenderSystem;
import mcjty.lib.client.BatchQuadGuiRenderer;
import mcjty.lib.client.GuiTools;
import mcjty.lib.client.RenderHelper;
import mcjty.lib.gui.*;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.varia.ComponentFactory;
import mcjty.lostradar.LostRadar;
import mcjty.lostradar.data.*;
import mcjty.lostradar.network.Messages;
import mcjty.lostradar.network.PacketStartSearch;
import mcjty.lostradar.setup.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static mcjty.lib.gui.widgets.Widgets.positional;

public class GuiRadar extends GuiItemScreen implements IKeyReceiver {

    private static final int xSize = 340;
    private static final int ySize = 236;

    private static final int MAPCELL_SIZE = 10;
    private static final int MAP_DIM = 10;

    private static final ResourceLocation ICONS = new ResourceLocation(LostRadar.MODID, "textures/gui/icons.png");
    private static final ResourceLocation MAP_ICONS_LOCATION = new ResourceLocation("textures/map/map_icons.png");

    private WidgetList categoryList;
    private Button scanButton;

    private final List<Pair<Rect2i, ChunkPos>> borderCoordinates = new ArrayList<>();

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
        categoryList = Widgets.list(238, 12, 93, ySize - 53);
        scanButton = Widgets.button(238, ySize - 40, 93, 15, "Scan")
                .event(() -> {
                    int selected = categoryList.getSelected();
                    if (selected >= 0) {
                        PaletteCache palette = PaletteCache.getOrCreatePaletteCache(MapPalette.getDefaultPalette(Minecraft.getInstance().level));
                        MapPalette.PaletteEntry entry = palette.getPalette().palette().get(selected);
                        String searchText = entry.name();
                        if (!searchText.isEmpty()) {
                            Messages.sendToServer(new PacketStartSearch(searchText));
                        }
                        ClientMapData.getData().setSearchString(searchText);
                        ClientMapData.getData().clearSearchResults();
                    }
                });
        Button clearButton = Widgets.button(238, ySize - 22, 93, 15, "Clear").event(() -> {
            ClientMapData.getData().clearSearchResults();
            ClientMapData.getData().setSearchString("");
            Messages.sendToServer(new PacketStartSearch(""));
            categoryList.selected(-1);
        });
        toplevel.children(categoryList, scanButton, clearButton);
        toplevel.bounds(k, l, xSize, ySize);
        populateCategoryList();

        window = new Window(this, toplevel);
    }

    private void renderMap(GuiGraphics graphics) {
        BatchQuadGuiRenderer batch = new BatchQuadGuiRenderer();

        ClientMapData data = ClientMapData.getData();
        ChunkPos p = new ChunkPos(Minecraft.getInstance().player.blockPosition());
        // For an area of 21x21 chunks around the player we render the color
        int borderLeft = this.guiLeft + 12;
        int borderTop = this.guiTop + 12;
        // Make a copy of searchResults so that we can modify it
        Set<ChunkPos> searchResults = new HashSet<>(data.getSearchResults());
        List<Icon> icons = renderCityGrid(batch, searchResults, p, data, borderLeft, borderTop);

        // Now render the remaining search results but at the border (just beyond the map)
        renderDistantSearchResults(batch, searchResults, p, borderLeft, borderTop);

        batch.render(graphics);

        // Get the angle from the player's rotation
        float angle = Minecraft.getInstance().player.getYRot() + 180;
        // Render the player as a white smaller dot
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        int startX = borderLeft + (MAP_DIM) * MAPCELL_SIZE;
        int startZ = borderTop + (MAP_DIM) * MAPCELL_SIZE;
        RenderHelper.drawRotatedIcon(graphics, startX + 2, startZ + 2, 16, angle, MAP_ICONS_LOCATION, 0, 0, 16, 16);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        for (Icon icon : icons) {
            graphics.blit(ICONS, icon.x(), icon.y(), icon.w(), icon.h(), icon.u(), icon.v(), icon.pw(), icon.ph(), 256, 256);
        }
    }

    private record Icon(int x, int y, int w, int h, int u, int v, int pw, int ph) {
    }

    private List<Icon> renderCityGrid(BatchQuadGuiRenderer batch, Set<ChunkPos> searchResults, ChunkPos p, ClientMapData data, int borderLeft, int borderTop) {
        Set<EntryPos> searchedChunks = data.getSearchedChunks();
        // Based on the current time calculate an rgb color between Config.HIGHLIGHT_x1 and Config.HIGHLIGHT_x2
        // Fluctuate back and forth between the two colors in a 2 second cycle
        float time = System.currentTimeMillis() % 4000 / 2000f;
        if (time > 1) {
            time = 2 - time;
        }
        int r = (int) (Config.HILIGHT_R1.get() + (Config.HILIGHT_R2.get() - Config.HILIGHT_R1.get()) * time);
        int g = (int) (Config.HILIGHT_G1.get() + (Config.HILIGHT_G2.get() - Config.HILIGHT_G1.get()) * time);
        int b = (int) (Config.HILIGHT_B1.get() + (Config.HILIGHT_B2.get() - Config.HILIGHT_B1.get()) * time);
        int highlightColor = 0xff000000 | (r << 16) | (g << 8) | b;

        List<Icon> icons = new ArrayList<>();
        for (int x = -MAP_DIM; x <= MAP_DIM; x++) {
            for (int z = -MAP_DIM; z <= MAP_DIM; z++) {
                ChunkPos pos = new ChunkPos(p.x + x, p.z + z);
                int biomeColor = data.getBiomeColor(Minecraft.getInstance().level, pos);
                if (biomeColor != -1) {
                    // Render the biome color
                    RenderHelper.drawBeveledBox(batch, borderLeft + (x+ MAP_DIM) * MAPCELL_SIZE, borderTop + (z+ MAP_DIM) * MAPCELL_SIZE, borderLeft + (x + MAP_DIM + 1) * MAPCELL_SIZE, borderTop + (z + MAP_DIM + 1) * MAPCELL_SIZE, 0xff000000 + biomeColor, 0xff000000 + biomeColor, 0xff000000 + biomeColor);
                }
                int startX = borderLeft + (x + MAP_DIM) * MAPCELL_SIZE;
                int startZ = borderTop + (z + MAP_DIM) * MAPCELL_SIZE;
                MapPalette.PaletteEntry entry = data.getPaletteEntry(Minecraft.getInstance().level, pos);
                if (entry != null) {
                    // Render the color
                    int color = entry.color();
                    int fullColor = 0xff000000 | (color & 0x00ffffff);
                    int borderColor = 0xff333333;
                    if (searchResults.contains(pos)) {
                        // This is a search result
                        borderColor = highlightColor;
                        searchResults.remove(pos);
                    }

                    if (entry == MapPalette.CITY) {
                        RenderHelper.drawBeveledBox(batch, startX, startZ, startX + MAPCELL_SIZE, startZ + MAPCELL_SIZE, fullColor, fullColor, fullColor);

                        // Determine pattern offset
                        int patternOffsetX = x % 2;
                        int patternOffsetZ = z % 2;

                        // Draw 5×5 dithered black squares
                        int step = MAPCELL_SIZE / 5;
                        for (int i = 0; i < 5; i++) {
                            for (int j = 0; j < 5; j++) {
                                // Simple dither condition — you can tweak this for different patterns
                                if ((i + j + patternOffsetX + patternOffsetZ) % 2 == 0) {
                                    int x0 = startX + i * step;
                                    int z0 = startZ + j * step;
                                    int x1 = startX + (i + 1) * step;
                                    int z1 = startZ + (j + 1) * step;
                                    RenderHelper.drawBeveledBox(batch, x0, z0, x1, z1, 0xff000000, 0xff000000, 0xff000000);
                                }
                            }
                        }
                    } else {
                        RenderHelper.drawBeveledBox(batch, borderLeft + (x + MAP_DIM) * MAPCELL_SIZE, borderTop + (z + MAP_DIM) * MAPCELL_SIZE, borderLeft + (x + MAP_DIM + 1) * MAPCELL_SIZE, borderTop + (z + MAP_DIM + 1) * MAPCELL_SIZE, borderColor, borderColor, 0xff000000 + color);
                    }
                    if (entry.iconU() >= 0) {
                        // We have an icon
                        icons.add(new Icon(startX+2, startZ+2, MAPCELL_SIZE-4, MAPCELL_SIZE-4, entry.iconU(), entry.iconV(), 32, 32));
                    }
                }

                // If we want to show searched areas we render a darker overlay on top of the map parts that we didn't search
                if (!searchedChunks.isEmpty() && !searchedChunks.contains(EntryPos.fromChunkPos(Minecraft.getInstance().level.dimension(), pos))) {
                    // Render a darker overlay
                    RenderHelper.drawBeveledBox(batch, borderLeft + (x + MAP_DIM) * MAPCELL_SIZE, borderTop + (z + MAP_DIM) * MAPCELL_SIZE, borderLeft + (x + MAP_DIM + 1) * MAPCELL_SIZE, borderTop + (z + MAP_DIM + 1) * MAPCELL_SIZE, 0x80000000, 0x80000000, 0x80000000);
                }
            }
        }
        return icons;
    }

    private void renderDistantSearchResults(BatchQuadGuiRenderer batch, Set<ChunkPos> searchResults, ChunkPos p, int borderLeft, int borderTop) {
        borderCoordinates.clear();
        for (ChunkPos pos : searchResults) {
            int dx = pos.x - p.x;
            int dz = pos.z - p.z;

            if (dx == 0 && dz == 0) continue; // Skip center

            int adx = Math.abs(dx);
            int adz = Math.abs(dz);
            int bx, bz;

            if (adx * MAP_DIM >= adz * MAP_DIM) {
                bx = Integer.signum(dx) * (MAP_DIM + 1);
                bz = (int) Math.round((double) dz / adx * (MAP_DIM + 1));
            } else {
                bz = Integer.signum(dz) * (MAP_DIM + 1);
                bx = (int) Math.round((double) dx / adz * (MAP_DIM + 1));
            }

            // Color based on distance to center:
            // - White when the search result is right at the center of the map
            // - Black when the search result is 80 chunks away
            // - Gray when the search result is in between
            int distance = Math.max(Math.abs(dx), Math.abs(dz));
            int minDistance = MAP_DIM + 1;
            int maxDistance = 80;
            int clamped = Math.max(0, Math.min(255, (int)(255 * (1 - (double)(distance - minDistance) / (maxDistance - minDistance)))));
            int color = 0xff000000 | (clamped << 16) | (clamped << 8) | clamped;
            int x1 = borderLeft + (bx + MAP_DIM) * MAPCELL_SIZE + 3;
            int y1 = borderTop + (bz + MAP_DIM) * MAPCELL_SIZE + 3;
            RenderHelper.drawBeveledBox(batch, x1, y1, borderLeft + (bx + MAP_DIM + 1) * MAPCELL_SIZE - 3, borderTop + (bz + MAP_DIM + 1) * MAPCELL_SIZE - 3, color, color, color);
            // Store the coordinates for later use
            borderCoordinates.add(Pair.of(new Rect2i(x1, y1, MAPCELL_SIZE - 6, MAPCELL_SIZE - 6), pos));
        }
    }

    public static void refresh() {
        if (Minecraft.getInstance().screen instanceof GuiRadar radar) {
            radar.populateCategoryList();
        }
    }

    private void populateCategoryList() {
        ClientMapData data = ClientMapData.getData();
        String searchString = data.getSearchString();
        AtomicInteger selected = new AtomicInteger(categoryList.getSelected());
        categoryList.removeChildren();
        PaletteCache palette = PaletteCache.getOrCreatePaletteCache(MapPalette.getDefaultPalette(Minecraft.getInstance().level));
        PlayerMapKnowledgeDispatcher.getPlayerMapKnowledge(Minecraft.getInstance().player).ifPresent(handler -> {
            for (MapPalette.PaletteEntry category : palette.getPalette().palette()) {
                if (handler.getKnownCategories().contains(category.name())) {
                    categoryList.children(makeLine(category));
                    if (!searchString.isEmpty() && category.name().equals(searchString)) {
                        selected.set(categoryList.getChildren().size() - 1);
                    }
                }
            }
        });
        categoryList.selected(selected.get());
    }

    private Widget<Label> makeLine(MapPalette.PaletteEntry category) {
        return Widgets.label(category.name());
    }

    @Override
    protected void renderInternal(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        ClientMapData data = ClientMapData.getData();
        boolean scanEnabled = categoryList.getSelected() >= 0;
        scanButton.enabled(scanEnabled);
        int progress = data.getSearchProgress();
        if (progress >= 100) {
            scanButton.text("Scan");
        } else {
            scanButton.text(progress + "%");
        }
        drawWindow(graphics);
        renderMap(graphics);
        renderTooltip(graphics, mouseX, mouseY);
    }

    private void renderTooltip(@Nonnull GuiGraphics graphics, int xxmouseX, int yymouseY) {
        int mouseX = GuiTools.getRelativeX(this);
        int mouseY = GuiTools.getRelativeY(this);

        int borderLeft = this.guiLeft + 12;
        int borderTop = this.guiTop + 12;

        ClientMapData data = ClientMapData.getData();

        ChunkPos p = new ChunkPos(Minecraft.getInstance().player.blockPosition());
        int tooltipX = mouseX - 20;
        int tooltipY = mouseY - 3;
        if (tooltipY < 14) {
            tooltipY = mouseY + 20;
        }

        // Check that the mouse position is on the map
        if (mouseX < borderLeft || mouseX > borderLeft + MAPCELL_SIZE * (MAP_DIM * 2 + 1) || mouseY < borderTop || mouseY > borderTop + MAPCELL_SIZE * (MAP_DIM * 2 + 1)) {
            // Mouse is outside the map area. Check if it is on the border
            for (Pair<Rect2i, ChunkPos> pair : borderCoordinates) {
                Rect2i rect = pair.getKey();
                ChunkPos pos = pair.getValue();
                // Check if mouseX and mouseY are within the rectangle
                if (rect.contains(mouseX, mouseY)) {
                    String posString = String.format("%d, %d", pos.getMiddleBlockX(), pos.getMiddleBlockZ());
                    String distanceString = String.format("%d", Math.max(Math.abs(pos.x - p.x), Math.abs(pos.z - p.z)) * 16);
                    List<Component> components = List.of(ComponentFactory.translatable("lostradar.chunk.pos", posString),
                            ComponentFactory.translatable("lostradar.chunk.dist", distanceString));
                    graphics.renderTooltip(Minecraft.getInstance().font, components, Optional.empty(),
                            tooltipX, tooltipY);
                    break;
                }
            }
        } else {
            // Find the palette entry at the mouse position (x, y)
            ChunkPos pos = new ChunkPos(
                    p.x + (mouseX - borderLeft) / MAPCELL_SIZE - MAP_DIM,
                    p.z + (mouseY - borderTop) / MAPCELL_SIZE - MAP_DIM);
            MapPalette.PaletteEntry entry = data.getPaletteEntry(Minecraft.getInstance().level, pos);
            if (entry != null) {
                graphics.renderTooltip(Minecraft.getInstance().font, ComponentFactory.translatable(entry.translatableKey()), tooltipX, tooltipY);
            }
        }
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
    }

    public static void open() {
        Minecraft.getInstance().setScreen(new GuiRadar());
    }
}
