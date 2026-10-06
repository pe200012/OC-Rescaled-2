package ocsquared.server.fs

trait Volatile extends VirtualFileSystem {
  override def close():Unit = {
    super.close()
    root.children.clear()
  }
}
