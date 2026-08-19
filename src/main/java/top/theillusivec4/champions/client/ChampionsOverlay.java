package top.theillusivec4.champions.client;

import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import top.theillusivec4.champions.client.util.HUDHelper;
import top.theillusivec4.champions.client.util.MouseHelper;
import top.theillusivec4.champions.common.config.ChampionsConfig;

public class ChampionsOverlay implements IGuiOverlay {

  public static boolean isRendering = false;
  public static int startX = 0;
  public static int startY = 0;

  @Override
  public void render(net.minecraftforge.client.gui.overlay.ForgeGui gui, GuiGraphics guiGraphics,
                     float partialTick, int width, int height) {

    if (ChampionsConfig.showHud) {
      Minecraft mc = Minecraft.getInstance();
      Optional<LivingEntity> livingEntity =
        MouseHelper.getMouseOverChampion(mc, partialTick);
      livingEntity.ifPresent(
          entity -> isRendering = HUDHelper.renderHealthBar(guiGraphics, entity));

      if (livingEntity.isEmpty()) {
        isRendering = false;
      }
    }
  }
}
