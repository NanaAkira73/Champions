/*
 * Copyright (C) 2018-2019  C4
 *
 * This file is part of Champions, a mod made for Minecraft.
 *
 * Champions is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Champions is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Champions.  If not, see <https://www.gnu.org/licenses/>.
 */

package top.theillusivec4.champions;

import com.electronwill.nightconfig.core.CommentedConfig;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import top.theillusivec4.champions.common.particle.RankParticle.RankFactory;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.IConfigSpec;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.theillusivec4.champions.api.IChampion;
import top.theillusivec4.champions.api.IChampionsApi;
import top.theillusivec4.champions.api.impl.ChampionsApiImpl;
import top.theillusivec4.champions.client.ChampionsOverlay;
import top.theillusivec4.champions.client.affix.ClientAffixEventsHandler;
import top.theillusivec4.champions.client.config.ClientChampionsConfig;
import top.theillusivec4.champions.common.affix.core.AffixManager;
import top.theillusivec4.champions.common.capability.ChampionCapability;
import top.theillusivec4.champions.common.config.ChampionsConfig;
import top.theillusivec4.champions.common.item.ChampionEggItem;
import top.theillusivec4.champions.common.loot.LootItemChampionPropertyCondition;
import top.theillusivec4.champions.common.network.NetworkHandler;
import top.theillusivec4.champions.common.rank.RankManager;
import top.theillusivec4.champions.common.registry.ChampionsRegistry;
import top.theillusivec4.champions.common.registry.RegistryReference;
import top.theillusivec4.champions.common.stat.ChampionsStats;
import top.theillusivec4.champions.common.util.EntityManager;
import top.theillusivec4.champions.server.command.AffixArgument;
import top.theillusivec4.champions.server.command.ChampionSelectorOptions;

@Mod(Champions.MODID)
public class Champions {

  public static final String MODID = "champions";
  public static final Logger LOGGER = LogManager.getLogger();
  public static final IChampionsApi API = ChampionsApiImpl.getInstance();

  public Champions() {
    ModLoadingContext.get().registerConfig(Type.CLIENT, ClientChampionsConfig.CLIENT_SPEC);
    ModLoadingContext.get().registerConfig(Type.SERVER, ChampionsConfig.SERVER_SPEC);
    createServerConfig(ChampionsConfig.RANKS_SPEC, "ranks");
    createServerConfig(ChampionsConfig.AFFIXES_SPEC, "affixes");
    createServerConfig(ChampionsConfig.ENTITIES_SPEC, "entities");
    IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
    ChampionsRegistry.REGISTERS.forEach(register -> register.register(eventBus));
    eventBus.addListener(this::config);
    eventBus.addListener(this::setup);
    eventBus.addListener(this::clientSetup);
    eventBus.addListener(this::registerParticleFactories);
    eventBus.addListener(this::registerOverlays);
    eventBus.addListener(this::registerCaps);
  }

  private void setup(final FMLCommonSetupEvent evt) {
    ChampionCapability.register();
    NetworkHandler.register();
    AffixManager.register();
    evt.enqueueWork(() -> {
      ChampionsStats.setup();
      ChampionSelectorOptions.setup();
      DispenseItemBehavior dispenseBehavior = (source, stack) -> {
        Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);
        Optional<EntityType<?>> entitytype = ChampionEggItem.getType(stack);
        entitytype.ifPresent(type -> {
          Entity entity = type.create((ServerLevel) source.getLevel(), stack.getTag(), null,
            source.getPos().relative(direction), MobSpawnType.DISPENSER, true,
            direction != Direction.UP);

          if (entity instanceof LivingEntity) {
            ChampionCapability.getCapability(entity)
              .ifPresent(champion -> ChampionEggItem.read(champion, stack));
            source.getLevel().addFreshEntity(entity);
            stack.shrink(1);
          }
        });
        return stack;
      };
      DispenserBlock.registerBehavior(ChampionsRegistry.EGG.get(), dispenseBehavior);
    });
  }

  @SuppressWarnings("unused")
  private void clientSetup(final FMLClientSetupEvent evt) {
    MinecraftForge.EVENT_BUS.register(new ClientAffixEventsHandler());
    Minecraft.getInstance().getItemColors()
      .register(ChampionEggItem::getColor, ChampionsRegistry.EGG.get());
  }

  private void registerParticleFactories(final RegisterParticleProvidersEvent evt) {
    evt.registerSpriteSet(ChampionsRegistry.RANK.get(), RankFactory::new);
  }

  private void registerOverlays(final RegisterGuiOverlaysEvent evt) {
    evt.registerAboveAll("champions_health_bar", new ChampionsOverlay());
  }

  private void registerCaps(final RegisterCapabilitiesEvent evt) {
    evt.register(IChampion.class);
  }

  private void config(final ModConfigEvent evt) {

    if (evt.getConfig().getModId().equals(MODID)) {

      if (evt.getConfig().getType() == Type.SERVER) {
        synchronized (this) {
          ChampionsConfig.bake();
          IConfigSpec<?> spec = evt.getConfig().getSpec();
          CommentedConfig commentedConfig = evt.getConfig().getConfigData();

          if (spec == ChampionsConfig.RANKS_SPEC) {
            ChampionsConfig.transformRanks(commentedConfig);
            RankManager.buildRanks();
          } else if (spec == ChampionsConfig.AFFIXES_SPEC) {
            ChampionsConfig.transformAffixes(commentedConfig);
            AffixManager.buildAffixSettings();
          } else if (spec == ChampionsConfig.ENTITIES_SPEC) {
            ChampionsConfig.transformEntities(commentedConfig);
            EntityManager.buildEntitySettings();
          }
        }
      } else if (evt.getConfig().getType() == Type.CLIENT) {
        ClientChampionsConfig.bake();
      }
    }
  }

  private static void createServerConfig(ForgeConfigSpec spec, String suffix) {
    String fileName = "champions-" + suffix + ".toml";
    ModLoadingContext.get().registerConfig(Type.SERVER, spec, fileName);
    File defaults = new File(FMLPaths.GAMEDIR.get() + "/defaultconfigs/" + fileName);

    if (!defaults.exists()) {
      try {
        FileUtils.copyInputStreamToFile(
          Objects.requireNonNull(Champions.class.getClassLoader().getResourceAsStream(fileName)),
          defaults);
      } catch (IOException e) {
        LOGGER.error("Error creating default config for " + fileName);
      }
    }
  }
}
