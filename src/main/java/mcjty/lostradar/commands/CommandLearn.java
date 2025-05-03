package mcjty.lostradar.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcjty.lostradar.data.PlayerMapKnowledgeDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class CommandLearn {

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("learn")
                .requires(cs -> cs.hasPermission(2))
                .then(Commands.argument("category", StringArgumentType.word())
                                .executes(CommandLearn::learnCategory));
    }

    private static int learnCategory(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String category = context.getArgument("category", String.class);
        ServerPlayer player = context.getSource().getPlayerOrException();
        PlayerMapKnowledgeDispatcher.getPlayerMapKnowledge(player).ifPresent(handler -> {
            handler.getKnownCategories().add(category);
        });
        return 0;
    }
}