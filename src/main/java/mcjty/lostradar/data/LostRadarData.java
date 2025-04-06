package mcjty.lostradar.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class LostRadarData extends SavedData {

    public static final String NAME = "LostRadarData";

//    public static Map<ChunkPos, ClientData> chunkPosMap = new HashMap<>();

    @Nonnull
    public static LostRadarData getData(Level world) {
        if (world.isClientSide) {
            throw new RuntimeException("Don't access this client-side!");
        }
        DimensionDataStorage storage = ((ServerLevel)world).getDataStorage();
        return storage.computeIfAbsent(LostRadarData::new, LostRadarData::new, NAME);
    }

    public LostRadarData() {
    }

    public LostRadarData(CompoundTag tag) {
        load(tag);
    }

    private void load(CompoundTag nbt) {
        ListTag list = nbt.getList("chunks", Tag.TAG_COMPOUND);
        for (Tag tag : list) {
            CompoundTag tc = (CompoundTag) tag;
        }
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        ListTag list = new ListTag();
//        for (Map.Entry<ChunkCoord, LostChunkData> entry : lostChunkDataMap.entrySet()) {
//            CompoundTag tc = new CompoundTag();
//        }
        compound.put("chunks", list);
        return compound;
    }
}
