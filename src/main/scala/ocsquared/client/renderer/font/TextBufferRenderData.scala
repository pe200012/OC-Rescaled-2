package ocsquared.client.renderer.font

import ocsquared.util.TextBuffer

trait TextBufferRenderData {
  def dirty: Boolean

  def dirty_=(value: Boolean): Unit

  def data: TextBuffer

  def viewport: (Int, Int)
}
