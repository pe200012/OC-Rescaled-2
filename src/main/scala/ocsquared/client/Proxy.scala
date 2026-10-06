package ocsquared.client

import ocsquared.OpenComputers
import li.cil.oc.api
import ocsquared.client
import ocsquared.client.renderer.HighlightRenderer
import ocsquared.client.renderer.MFUTargetRenderer
import ocsquared.client.renderer.PetRenderer
import ocsquared.client.renderer.TextBufferRenderCache
import ocsquared.client.renderer.WirelessNetworkDebugRenderer
import ocsquared.client.renderer.block.ModelInitialization
import ocsquared.client.renderer.block.NetSplitterModel
import ocsquared.client.renderer.entity.DroneRenderer
import ocsquared.client.renderer.tileentity._
import ocsquared.common.component.TextBuffer
import ocsquared.common.entity.Drone
import ocsquared.common.event.NanomachinesHandler
import ocsquared.common.event.RackMountableRenderHandler
import ocsquared.common.item.traits.Delegate
import ocsquared.common.tileentity
import ocsquared.common.{Proxy => CommonProxy}
import ocsquared.util.Audio
import net.minecraft.block.Block
import net.minecraft.client.renderer.entity.{Render, RenderManager}
import net.minecraft.item.Item
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.client.registry.{ClientRegistry, IRenderFactory, RenderingRegistry}
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.network.NetworkRegistry
import org.lwjgl.opengl.GLContext

private[ocsquared] class Proxy extends CommonProxy {
  override def preInit(e: FMLPreInitializationEvent): Unit = {
    super.preInit(e)

    api.API.manual = client.Manual

    CommandHandler.register()

    MinecraftForge.EVENT_BUS.register(Textures)
    MinecraftForge.EVENT_BUS.register(NetSplitterModel)

    ModelInitialization.preInit()

    RenderingRegistry.registerEntityRenderingHandler(classOf[Drone], new IRenderFactory[Drone] {
      override def createRenderFor(manager: RenderManager): Render[? >: Drone] = new DroneRenderer(manager)
    })
  }

  override def init(e: FMLInitializationEvent) : Unit = {
    super.init(e)

    OpenComputers.channel.register(client.PacketHandler)

    ColorHandler.init()

    ClientRegistry.bindTileEntitySpecialRenderer(classOf[tileentity.Adapter], AdapterRenderer)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[tileentity.Assembler], AssemblerRenderer)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[tileentity.Case], CaseRenderer)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[tileentity.Charger], ChargerRenderer)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[tileentity.Disassembler], DisassemblerRenderer)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[tileentity.DiskDrive], DiskDriveRenderer)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[tileentity.Geolyzer], GeolyzerRenderer)
    if (GLContext.getCapabilities.OpenGL15)
      ClientRegistry.bindTileEntitySpecialRenderer(classOf[tileentity.Hologram], HologramRenderer)
    else
      ClientRegistry.bindTileEntitySpecialRenderer(classOf[tileentity.Hologram], HologramRendererFallback)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[tileentity.Microcontroller], MicrocontrollerRenderer)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[tileentity.NetSplitter], NetSplitterRenderer)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[tileentity.PowerDistributor], PowerDistributorRenderer)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[tileentity.Printer], PrinterRenderer)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[tileentity.Raid], RaidRenderer)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[tileentity.Rack], RackRenderer)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[tileentity.Relay], RelayRenderer)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[tileentity.RobotProxy], RobotRenderer)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[tileentity.Screen], ScreenRenderer)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[tileentity.Transposer], TransposerRenderer)

    ClientRegistry.registerKeyBinding(KeyBindings.clipboardPaste)

    MinecraftForge.EVENT_BUS.register(HighlightRenderer)
    MinecraftForge.EVENT_BUS.register(NanomachinesHandler.Client)
    MinecraftForge.EVENT_BUS.register(PetRenderer)
    MinecraftForge.EVENT_BUS.register(RackMountableRenderHandler)
    MinecraftForge.EVENT_BUS.register(Sound)
    MinecraftForge.EVENT_BUS.register(TextBuffer)
    MinecraftForge.EVENT_BUS.register(MFUTargetRenderer)
    MinecraftForge.EVENT_BUS.register(WirelessNetworkDebugRenderer)

    NetworkRegistry.INSTANCE.registerGuiHandler(OpenComputers, GuiHandler)

    MinecraftForge.EVENT_BUS.register(Audio)
    MinecraftForge.EVENT_BUS.register(HologramRenderer)
    MinecraftForge.EVENT_BUS.register(PetRenderer)
    MinecraftForge.EVENT_BUS.register(Sound)
    MinecraftForge.EVENT_BUS.register(TextBufferRenderCache)
  }

  override def registerModel(instance: Delegate, id: String): Unit = ModelInitialization.registerModel(instance, id)

  override def registerModel(instance: Item, id: String): Unit = ModelInitialization.registerModel(instance, id)

  override def registerModel(instance: Block, id: String): Unit = ModelInitialization.registerModel(instance, id)
}
