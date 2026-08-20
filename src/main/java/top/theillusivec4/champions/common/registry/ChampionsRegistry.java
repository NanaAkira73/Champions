package top.theillusivec4.champions.common.registry;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import top.theillusivec4.champions.Champions;
import top.theillusivec4.champions.common.entity.ArcticBulletEntity;
import top.theillusivec4.champions.common.entity.EnkindlingBulletEntity;
import top.theillusivec4.champions.common.item.ChampionEggItem;
import top.theillusivec4.champions.common.loot.ChampionLootModifier;
import top.theillusivec4.champions.common.loot.EntityIsChampion;
import top.theillusivec4.champions.common.loot.LootItemChampionPropertyCondition;
import top.theillusivec4.champions.common.potion.ParalysisEffect;
import top.theillusivec4.champions.common.potion.WoundEffect;

public class ChampionsRegistry {

  public static final DeferredRegister<Item> ITEMS =
      DeferredRegister.create(ForgeRegistries.ITEMS, Champions.MODID);
  public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
      DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Champions.MODID);
  public static final DeferredRegister<MobEffect> MOB_EFFECTS =
      DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Champions.MODID);
  public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
      DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Champions.MODID);
  public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> GLOBAL_LOOT_MODIFIERS =
      DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Champions.MODID);
  public static final DeferredRegister<LootItemConditionType> LOOT_CONDITION_TYPES =
      DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, Champions.MODID);

  public static final RegistryObject<Item> EGG = ITEMS.register("egg", ChampionEggItem::new);

  public static final RegistryObject<SimpleParticleType> RANK =
      PARTICLE_TYPES.register("rank", () -> new SimpleParticleType(false));

  public static final RegistryObject<MobEffect> PARALYSIS =
      MOB_EFFECTS.register("paralysis", ParalysisEffect::new);

  public static final RegistryObject<MobEffect> WOUND =
      MOB_EFFECTS.register("wound", WoundEffect::new);

  public static final RegistryObject<EntityType<ArcticBulletEntity>> ARCTIC_BULLET =
      ENTITY_TYPES.register("arctic_bullet", () -> EntityType.Builder
          .of((EntityType<ArcticBulletEntity> type, Level level) -> new ArcticBulletEntity(type, level), MobCategory.MISC)
          .sized(0.3125F, 0.3125F).build("arctic_bullet"));

  public static final RegistryObject<EntityType<EnkindlingBulletEntity>> ENKINDLING_BULLET =
      ENTITY_TYPES.register("enkindling_bullet", () -> EntityType.Builder
          .of((EntityType<EnkindlingBulletEntity> type, Level level) -> new EnkindlingBulletEntity(type, level), MobCategory.MISC)
          .sized(0.3125F, 0.3125F).build("enkindling_bullet"));

  public static final RegistryObject<Codec<ChampionLootModifier>> CHAMPION_LOOT =
      GLOBAL_LOOT_MODIFIERS.register("champion_loot", () -> ChampionLootModifier.CODEC.get());

  public static final RegistryObject<LootItemConditionType> CHAMPION_PROPERTIES =
      LOOT_CONDITION_TYPES.register("champion_properties",
          () -> LootItemChampionPropertyCondition.INSTANCE);

  public static final RegistryObject<LootItemConditionType> IS_CHAMPION =
      LOOT_CONDITION_TYPES.register("entity_champion", () -> EntityIsChampion.TYPE);

  public static final List<DeferredRegister<?>> REGISTERS =
      List.of(ITEMS, PARTICLE_TYPES, MOB_EFFECTS, ENTITY_TYPES, GLOBAL_LOOT_MODIFIERS,
          LOOT_CONDITION_TYPES);
}
