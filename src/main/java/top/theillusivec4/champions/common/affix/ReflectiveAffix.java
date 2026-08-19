package top.theillusivec4.champions.common.affix;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import top.theillusivec4.champions.api.AffixCategory;
import top.theillusivec4.champions.api.IChampion;
import top.theillusivec4.champions.common.affix.core.BasicAffix;
import top.theillusivec4.champions.common.config.ChampionsConfig;

public class ReflectiveAffix extends BasicAffix {

  public ReflectiveAffix() {
    super("reflective", AffixCategory.OFFENSE, true);
  }

  @SubscribeEvent
  public void onDamageEvent(LivingDamageEvent evt) {
    if (!ChampionsConfig.reflectiveLethal && evt.getSource().getEntity() == evt.getEntity()) {
      LivingEntity living = evt.getEntity();
      float currentDamage = evt.getAmount();

      if (currentDamage >= living.getHealth()) {
        evt.setAmount(living.getHealth() - 1);
      }
    }
  }

  @Override
  public float onDamage(IChampion champion, DamageSource source, float amount, float newAmount) {

    if (source.getDirectEntity() instanceof LivingEntity sourceEntity) {

      if (source.getEntity() == champion.getLivingEntity()) {
        return newAmount;
      }
      DamageSource newSource =
          champion.getLivingEntity().damageSources().thorns(champion.getLivingEntity());
      float min = (float) ChampionsConfig.reflectiveMinPercent;
      float damage = (float) Math.min(
        amount *
          (sourceEntity.getRandom().nextFloat() * (ChampionsConfig.reflectiveMaxPercent - min)
            + min), ChampionsConfig.reflectiveMax);
      sourceEntity.hurt(newSource, damage);
    }
    return newAmount;
  }
}
