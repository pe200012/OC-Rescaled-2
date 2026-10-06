package ocsquared.client.renderer.entity

import ocsquared.client.Textures
import ocsquared.common.entity.Drone
import ocsquared.util.RenderState
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.entity.{Render, RenderManager}
import net.minecraft.util.ResourceLocation

class DroneRenderer(manager: RenderManager) extends Render[Drone](manager) {
  val model = new ModelQuadcopter()

  override def doRender(entity: Drone, x: Double, y: Double, z: Double, yaw: Float, dt: Float):Unit = {
    bindEntityTexture(entity)
    GlStateManager.pushMatrix()
    RenderState.pushAttrib()

    GlStateManager.translate(x, y + 2 / 16f, z)

    model.render(entity, 0, 0, 0, 0, 0, dt)

    RenderState.popAttrib()
    GlStateManager.popMatrix()
  }

  override def getEntityTexture(entity: Drone): ResourceLocation = Textures.Model.Drone
}
