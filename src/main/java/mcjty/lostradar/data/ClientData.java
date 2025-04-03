package mcjty.lostradar.data;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;

import java.util.HashMap;
import java.util.Map;

public class ClientData {

    public static Map<ChunkPos, ClientData> chunkPosMap = new HashMap<>();
}
