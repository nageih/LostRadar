package mcjty.lostradar.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcjty.lostradar.data.PlayerMapKnowledge;
import mcjty.lostradar.data.PlayerMapKnowledgeDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class CommandClear {

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("clear")
                .requires(cs -> cs.hasPermission(1))
                .executes(CommandClear::run);
    }


    private static int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        PlayerMapKnowledgeDispatcher.getPlayerMapKnowledge(player).ifPresent(PlayerMapKnowledge::clearKnowledge);
        return 0;
    }
}
