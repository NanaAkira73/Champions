package top.theillusivec4.champions.common.entity;

import javax.annotation.Nonnull;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import top.theillusivec4.champions.common.registry.ChampionsRegistry;

public class EnkindlingBulletEntity extends BaseBulletEntity {

  public EnkindlingBulletEntity(EntityType<? extends EnkindlingBulletEntity> type, Level level) {
    super(type, level);
  }

  public EnkindlingBulletEntity(Level level, LivingEntity livingEntity, @Nonnull Entity entity,
                                Direction.Axis axis) {
    super(ChampionsRegistry.ENKINDLING_BULLET.get(), level, livingEntity, entity, axis);
  }

  @Override
  protected void bulletEffect(LivingEntity target) {

    if (this.getOwner() != null) {
      target.hurt(this.damageSources().thrown(this, this.getOwner()), 1);
    } else {
      target.hurt(this.damageSources().thrown(this, this), 1);
    }
    target.setSecondsOnFire(8);
  }

  @Override
  protected ParticleOptions getParticle() {
    return ParticleTypes.FLAME;
  }
}
