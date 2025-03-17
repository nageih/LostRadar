package mcjty.lostradar.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import mcjty.lostradar.LostRadar;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class ModCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> commands = dispatcher.register(
                Commands.literal(LostRadar.MODID)
                        .then(CommandLearn.register(dispatcher))
                        .then(CommandList.register(dispatcher))
        );

        dispatcher.register(Commands.literal("ls").redirect(commands));
    }

}
