package ocsquared.client.renderer

import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

import com.google.common.cache.CacheBuilder
import com.google.common.cache.RemovalListener
import com.google.common.cache.RemovalNotification
import ocsquared.Settings
import ocsquared.client.renderer.font.TextBufferRenderData
import ocsquared.common.component
import ocsquared.util.RenderState
import net.minecraft.client.renderer.GLAllocation
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.tileentity.TileEntity
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import org.lwjgl.opengl.GL11

object TextBufferRenderCache extends Callable[Int] with RemovalListener[TileEntity, Int] {
  val renderer =
    if (Settings.get.fontRenderer == "texture") new font.StaticFontRenderer()
    else new font.DynamicFontRenderer()

  private val cache = com.google.common.cache.CacheBuilder.newBuilder().
    expireAfterAccess(2, TimeUnit.SECONDS).
    removalListener(this).
    asInstanceOf[CacheBuilder[TextBufferRenderData, Int]].
    build[TextBufferRenderData, Int]()

  // To allow access in cache entry init.
  private var currentBuffer: TextBufferRenderData = scala.compiletime.uninitialized

  // The pixels of screens showing a RISC-V machine's framebuffer instead of text. Made again from
  // the screen's copy when it was not drawn for a while.
  private val pixelTextures = CacheBuilder.newBuilder().
    expireAfterAccess(2, TimeUnit.SECONDS).
    removalListener(new RemovalListener[component.TextBuffer, DynamicTexture] {
      override def onRemoval(e: RemovalNotification[component.TextBuffer, DynamicTexture]): Unit = e.getValue.deleteGlTexture()
    }).
    build[component.TextBuffer, DynamicTexture]()

  // ----------------------------------------------------------------------- //
  // Rendering
  // ----------------------------------------------------------------------- //

  def render(buffer: TextBufferRenderData):Unit = {
    currentBuffer = buffer
    compileOrDraw(cache.get(currentBuffer, this))
  }

  /**
   * Draws the pixels a screen shows instead of its text, scaled to fit where the text would be.
   * Returns whether they changed since they were last drawn.
   */
  def renderPixels(buffer: component.TextBuffer): Boolean = {
    val width = buffer.pixelWidth
    val height = buffer.pixelHeight
    var texture = pixelTextures.getIfPresent(buffer)
    if (texture != null && texture.getTextureData.length != width * height) {
      pixelTextures.invalidate(buffer)
      texture = null
    }
    val changed = buffer.pixelsChanged
    if (texture == null || changed) {
      if (texture == null) {
        texture = new DynamicTexture(width, height)
        pixelTextures.put(buffer, texture)
      }
      val data = texture.getTextureData
      for (i <- data.indices) {
        data(i) = 0xFF000000 | buffer.pixels(i)
      }
      texture.updateDynamicTexture()
      buffer.pixelsChanged = false
    }

    val areaWidth = buffer.renderWidth.toDouble
    val areaHeight = buffer.renderHeight.toDouble
    val scale = math.min(areaWidth / width, areaHeight / height)
    val x0 = (areaWidth - width * scale) / 2
    val y0 = (areaHeight - height * scale) / 2
    val x1 = x0 + width * scale
    val y1 = y0 + height * scale

    GlStateManager.depthMask(false)
    GlStateManager.enableTexture2D()
    GL11.glEnable(GL11.GL_TEXTURE_2D)
    RenderState.bindTexture(texture.getGlTextureId)
    GlStateManager.color(1, 1, 1, 1)
    GL11.glBegin(GL11.GL_QUADS)
    GL11.glTexCoord2d(0, 1)
    GL11.glVertex3d(x0, y1, 0)
    GL11.glTexCoord2d(1, 1)
    GL11.glVertex3d(x1, y1, 0)
    GL11.glTexCoord2d(1, 0)
    GL11.glVertex3d(x1, y0, 0)
    GL11.glTexCoord2d(0, 0)
    GL11.glVertex3d(x0, y0, 0)
    GL11.glEnd()
    RenderState.bindTexture(0)
    GlStateManager.depthMask(true)

    RenderState.checkError(getClass.getName + ".renderPixels: leaving")

    changed
  }

  private def compileOrDraw(list: Int) = {
    if (currentBuffer.dirty) {
      RenderState.checkError(getClass.getName + ".compileOrDraw: entering (aka: wasntme)")

      for (line <- currentBuffer.data.buffer) {
        renderer.generateChars(line)
      }

      val doCompile = !RenderState.compilingDisplayList
      if (doCompile) {
        currentBuffer.dirty = false
        GL11.glNewList(list, GL11.GL_COMPILE_AND_EXECUTE)

        RenderState.checkError(getClass.getName + ".compileOrDraw: glNewList")
      }

      renderer.drawBuffer(currentBuffer.data, currentBuffer.viewport._1, currentBuffer.viewport._2)

      RenderState.checkError(getClass.getName + ".compileOrDraw: drawString")

      if (doCompile) {
        GL11.glEndList()

        RenderState.checkError(getClass.getName + ".compileOrDraw: glEndList")
      }

      RenderState.checkError(getClass.getName + ".compileOrDraw: leaving")

      true
    }
    else {
      GL11.glCallList(list)
      GlStateManager.enableTexture2D()
      GlStateManager.depthMask(true)
      GlStateManager.color(1, 1, 1, 1)

      // Because display lists and the GlStateManager don't like each other, apparently.
      GL11.glEnable(GL11.GL_TEXTURE_2D)
      RenderState.bindTexture(0)
      GL11.glDepthMask(true)
      GL11.glColor4f(1, 1, 1, 1)

      RenderState.disableBlend()

      RenderState.checkError(getClass.getName + ".compileOrDraw: glCallList")
    }
  }

  // ----------------------------------------------------------------------- //
  // Cache
  // ----------------------------------------------------------------------- //

  def call: Int = {
    RenderState.checkError(getClass.getName + ".call: entering (aka: wasntme)")

    val list = GLAllocation.generateDisplayLists(1)
    currentBuffer.dirty = true // Force compilation.

    RenderState.checkError(getClass.getName + ".call: leaving")

    list
  }

  def onRemoval(e: RemovalNotification[TileEntity, Int]):Unit = {
    RenderState.checkError(getClass.getName + ".onRemoval: entering (aka: wasntme)")

    GLAllocation.deleteDisplayLists(e.getValue)

    RenderState.checkError(getClass.getName + ".onRemoval: leaving")
  }

  // ----------------------------------------------------------------------- //
  // ITickHandler
  // ----------------------------------------------------------------------- //

  @SubscribeEvent
  def onTick(e: ClientTickEvent): Unit = {
    cache.cleanUp()
    pixelTextures.cleanUp()
  }
}
