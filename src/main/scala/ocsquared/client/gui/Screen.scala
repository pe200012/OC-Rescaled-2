package ocsquared.client.gui

import li.cil.oc.api
import ocsquared.client.renderer.TextBufferRenderCache
import ocsquared.client.renderer.gui.BufferRenderer
import ocsquared.common.component
import ocsquared.util.RenderState
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.input.Mouse

class Screen(val buffer: api.internal.TextBuffer, val hasMouse: Boolean, val hasKeyboardCallback: () => Boolean, val hasPower: () => Boolean) extends traits.InputBuffer {
  override protected def hasKeyboard = hasKeyboardCallback()

  override protected def bufferX = 8 + x

  override protected def bufferY = 8 + y

  private val bufferMargin = BufferRenderer.margin + BufferRenderer.innerMargin

  private var didClick = false

  private var x, y = 0

  private var mx, my = -1

  private var hoverX, hoverY = -1

  private var lastHover = 0L

  override def handleMouseInput() : Unit = {
    super.handleMouseInput()
    if (hasMouse && Mouse.hasWheel && Mouse.getEventDWheel != 0) {
      val mouseX = Mouse.getEventX * width / mc.displayWidth
      val mouseY = height - Mouse.getEventY * height / mc.displayHeight - 1
      toBufferCoordinates(mouseX, mouseY) match {
        case Some((bx, by)) =>
          val scroll = math.signum(Mouse.getEventDWheel)
          buffer.mouseScroll(bx, by, scroll, null)
        case _ => // Ignore when out of bounds.
      }
    }
  }

  override protected def mouseClicked(mouseX: Int, mouseY: Int, button: Int) : Unit = {
    super.mouseClicked(mouseX, mouseY, button)
    if (hasMouse) {
      if (button == 0 || button == 1) {
        clickOrDrag(mouseX, mouseY, button)
      }
    }
  }

  protected override def mouseClickMove(mouseX: Int, mouseY: Int, button: Int, timeSinceLast: Long) : Unit = {
    super.mouseClickMove(mouseX, mouseY, button, timeSinceLast)
    if (hasMouse && timeSinceLast > 10) {
      if (button == 0 || button == 1) {
        clickOrDrag(mouseX, mouseY, button)
      }
    }
  }

  override protected def mouseReleased(mouseX: Int, mouseY: Int, button: Int) : Unit = {
    super.mouseReleased(mouseX, mouseY, button)
    if (hasMouse && button >= 0) {
      if (didClick) {
        toBufferCoordinates(mouseX, mouseY) match {
          case Some((bx, by)) => buffer.mouseUp(bx, by, button, null)
          case _ => buffer.mouseUp(-1.0, -1.0, button, null)
        }
      }
      didClick = false
      mx = -1
      my = -1
    }
  }

  private def clickOrDrag(mouseX: Int, mouseY: Int, button: Int) : Unit = {
    toBufferCoordinates(mouseX, mouseY) match {
      case Some((bx, by)) if cell(bx, by) != (mx, my) =>
        if (mx >= 0 && my >= 0) buffer.mouseDrag(bx, by, button, null)
        else buffer.mouseDown(bx, by, button, null)
        didClick = true
        val (cx, cy) = cell(bx, by)
        mx = cx
        my = cy
      case _ =>
    }
  }

  private def toBufferCoordinates(mouseX: Int, mouseY: Int): Option[(Double, Double)] = {
    val bx = (mouseX - x - bufferMargin) / scale / TextBufferRenderCache.renderer.charRenderWidth
    val by = (mouseY - y - bufferMargin) / scale / TextBufferRenderCache.renderer.charRenderHeight
    val bw = buffer.getViewportWidth
    val bh = buffer.getViewportHeight
    if (bx >= 0 && by >= 0 && bx < bw && by < bh) Some((bx, by))
    else None
  }

  // Where a move counts: half characters (for high precision mode, sends some unnecessary packets
  // when not using it, but eh), or pixels while the screen shows those.
  private def cell(bx: Double, by: Double): (Int, Int) = buffer match {
    case target: component.TextBuffer if target.isShowingPixels =>
      ((bx * target.pixelWidth / buffer.getViewportWidth).toInt, (by * target.pixelHeight / buffer.getViewportHeight).toInt)
    case _ => (bx.toInt, (by * 2).toInt)
  }

  // Pixels follow the pointer between clicks too, at most once a tick.
  private def hover(mouseX: Int, mouseY: Int): Unit = buffer match {
    case target: component.TextBuffer if hasMouse && !didClick && target.isShowingPixels &&
      System.currentTimeMillis() - lastHover >= 50 =>
      toBufferCoordinates(mouseX, mouseY) match {
        case Some((bx, by)) if cell(bx, by) != (hoverX, hoverY) =>
          val (cx, cy) = cell(bx, by)
          hoverX = cx
          hoverY = cy
          lastHover = System.currentTimeMillis()
          target.mouseMove(bx, by, null)
        case _ =>
      }
    case _ =>
  }

  override def drawScreen(mouseX: Int, mouseY: Int, dt: Float): Unit = {
    super.drawScreen(mouseX, mouseY, dt)
    hover(mouseX, mouseY)
    drawBufferLayer()
  }

  override def drawBuffer() : Unit = {
    GlStateManager.translate(x.toFloat, y.toFloat, 0)
    BufferRenderer.drawBackground()
    if (hasPower()) {
      GlStateManager.translate(bufferMargin.toFloat, bufferMargin.toFloat, 0)
      GlStateManager.scale(scale, scale, 1)
      RenderState.makeItBlend()
      BufferRenderer.drawText(buffer)
    }
  }

  override protected def changeSize(w: Double, h: Double, recompile: Boolean) = {
    val bw = buffer.renderWidth
    val bh = buffer.renderHeight
    val scaleX = math.min(width / (bw + bufferMargin * 2.0), 1)
    val scaleY = math.min(height / (bh + bufferMargin * 2.0), 1)
    val scale = math.min(scaleX, scaleY)
    val innerWidth = (bw * scale).toInt
    val innerHeight = (bh * scale).toInt
    x = (width - (innerWidth + bufferMargin * 2)) / 2
    y = (height - (innerHeight + bufferMargin * 2)) / 2
    if (recompile) {
      BufferRenderer.compileBackground(innerWidth, innerHeight)
    }
    scale
  }
}
