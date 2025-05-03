package mcjty.lostradar.data;

import mcjty.lostcities.api.ILostCityInformation;
import mcjty.lostradar.compat.LostCitiesCompat;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.WorldWorkerManager;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BuildingFinderWorker implements WorldWorkerManager.IWorker {

    private final List<EntryPos> entries = Collections.synchronizedList(new ArrayList<>());

    @Override
    public boolean hasWork() {
        return true;
    }

    @Override
    public boolean doWork() {
        if (!entries.isEmpty()) {
            EntryPos entry = entries.remove(0);
            if (entry != null) {
                MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                Level level = server.getLevel(entry.level());
                ILostCityInformation info = LostCitiesCompat.lostCities.getLostInfo(level);
                if (info != null) {
                }
            }
        }
        return false;
    }
}
