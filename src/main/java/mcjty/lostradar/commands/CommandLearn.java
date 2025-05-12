package mcjty.lostradar.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import mcjty.lostradar.data.MapPalette;
import mcjty.lostradar.data.PaletteCache;
import mcjty.lostradar.data.PlayerMapKnowledgeDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nonnull;
import java.util.stream.Stream;

public class CommandLearn {

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("learn")
                .requires(cs -> cs.hasPermission(2))
                .then(Commands.argument("category", StringArgumentType.word())
                        .suggests(getCategorySuggestionProvider())
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

    @Nonnull
    private static SuggestionProvider<CommandSourceStack> getCategorySuggestionProvider() {
        return (context, builder) -> {
            Stream<MapPalette.PaletteEntry> stream = PaletteCache.getOrCreatePaletteCache(MapPalette.getDefaultPalette(context.getSource().getLevel())).getPalette().palette().stream();
            return SharedSuggestionProvider.suggest(stream.map(MapPalette.PaletteEntry::name), builder);
        };
    }
}