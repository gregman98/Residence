/**
 * Copyright (C) 2017 Zrips
 */

package com.bekvon.bukkit.cmiLib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CMIReflections {

    private static Class<?> CraftServerClass;
    private static Object CraftServer;
    private static Class<?> CraftItemStack;
    private static Class<?> Item;
    private static Class<?> IStack;
    private static Class<?> NBTTagCompound;
    private static Class<?> CraftContainer;
    private static Class<?> CraftContainers;
    private static Class<?> PacketPlayOutOpenWindow;
    private static Class<?> PacketPlayOutWorldParticles;
    private static Class<?> EnumParticle;
    private static Class<?> ParticleParam;
    private static Class<?> CraftParticle;
    private static Class<?> CraftPlayer;
    public static Class<?> nmsChatSerializer;
    private static Class<?> CraftBeehive;

    static {
	initialize();
    }

//    public ItemReflection() {
//	initialize();
//    }

    private static void initialize() {
	try {
	    if (!Version.isCurrentHigher(Version.v1_8_R2))
		nmsChatSerializer = getMinecraftClass("ChatSerializer");
	    else  // 1_8_R2 moved to IChatBaseComponent
		nmsChatSerializer = getMinecraftClass("IChatBaseComponent$ChatSerializer");
	} catch (Throwable e) {
	    e.printStackTrace();
	}
	try {
	    CraftBeehive = getBukkitClass("block.impl.CraftBeehive");
	} catch (Throwable e) {
	}
	try {
	    CraftPlayer = getBukkitClass("entity.CraftPlayer");
	} catch (Throwable e) {
	    e.printStackTrace();
	}
	if (Version.isCurrentHigher(Version.v1_12_R1)) {
	    try {
		CraftParticle = getBukkitClass("CraftParticle");
	    } catch (Throwable e) {
//	    e.printStackTrace();
	    }
	    try {
		ParticleParam = getMinecraftClass("ParticleParam");
	    } catch (Throwable e) {
//	    e.printStackTrace();
	    }
	}
	try {
	    CraftContainer = getMinecraftClass("Container");
	} catch (Throwable e) {
	}
	try {
	    CraftContainers = getMinecraftClass("Containers");
	} catch (Throwable e) {
	}

	try {
	    PacketPlayOutOpenWindow = getMinecraftClass("PacketPlayOutOpenWindow");
	} catch (Throwable e) {
	    e.printStackTrace();
	}
	try {
	    PacketPlayOutWorldParticles = getMinecraftClass("PacketPlayOutWorldParticles");
	} catch (Throwable e) {
	    e.printStackTrace();
	}
	try {
	    if (Version.isCurrentEqualOrLower(Version.v1_12_R1))
		EnumParticle = getMinecraftClass("EnumParticle");
	} catch (Throwable e) {
	    e.printStackTrace();
	}
	try {
	    NBTTagCompound = getMinecraftClass("NBTTagCompound");
	} catch (ClassNotFoundException | SecurityException | IllegalArgumentException e) {
	    e.printStackTrace();
	}
	try {
	    CraftServerClass = getBukkitClass("CraftServer");
	} catch (ClassNotFoundException | SecurityException | IllegalArgumentException e) {
	    e.printStackTrace();
	}
	try {
	    CraftServer = CraftServerClass.cast(Bukkit.getServer());
	} catch (SecurityException | IllegalArgumentException e) {
	    e.printStackTrace();
	}
	try {
	    CraftItemStack = getBukkitClass("inventory.CraftItemStack");
	} catch (ClassNotFoundException | SecurityException | IllegalArgumentException e) {
	    e.printStackTrace();
	}
	try {
	    Item = getMinecraftClass("Item");
	} catch (ClassNotFoundException | SecurityException | IllegalArgumentException e) {
	    e.printStackTrace();
	}
	try {
	    IStack = getMinecraftClass("ItemStack");
	} catch (ClassNotFoundException | SecurityException | IllegalArgumentException e) {
	    e.printStackTrace();
	}
    }

    public static String toJson(ItemStack item) {
	if (item == null)
	    return null;

	Object nmsStack = asNMSCopy(item);

	try {
	    Method meth = IStack.getMethod("save", NBTTagCompound);
	    Object res = meth.invoke(nmsStack, NBTTagCompound.newInstance());
	    return res.toString();
	} catch (Throwable e) {
	    e.printStackTrace();
	}

	return null;
    }

    public static ItemStack HideFlag(ItemStack item, int state) {
	Object nmsStack = asNMSCopy(item);
	try {
	    Method methTag = nmsStack.getClass().getMethod("getTag");
	    Object tag = methTag.invoke(nmsStack);
	    if (tag == null)
		tag = NBTTagCompound.newInstance();

	    Method meth = tag.getClass().getMethod("setInt", String.class, int.class);
	    meth.invoke(tag, "HideFlags", state);
	    Method meth2 = nmsStack.getClass().getMethod("setTag", NBTTagCompound);
	    meth2.invoke(nmsStack, tag);
	    return (ItemStack) asBukkitCopy(nmsStack);
	} catch (Exception e) {
	}
	return item;
    }

    private static Integer getActiveContainerId(Object entityplayer) {
	try {
	    Field field = entityplayer.getClass().getField("activeContainer");
	    Object container = CraftContainer.cast(field.get(entityplayer));
	    Field field2 = container.getClass().getField("windowId");
	    Object ids = field2.get(container);
	    return (int) ids;
	} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
	    e.printStackTrace();
	}

	return null;
    }

    private static Object getContainer(String name) {
	try {
	    Field field = CraftContainers.getDeclaredField(name);
	    return field.get(CraftContainers);
	} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
	    e.printStackTrace();
	}
	return null;
    }

    public static void updateInventoryTitle(Player p, String title) {
	if (title.length() > 32) {
	    title = title.substring(0, 31) + "~";
	}

	try {

	    if (Version.isCurrentEqualOrHigher(Version.v1_14_R1)) {
		Object entityplayer = CraftPlayer.getMethod("getHandle").invoke(p);

		Object s = getContainer("GENERIC_9X1");
		switch (p.getOpenInventory().getTopInventory().getSize()) {
		case 9:
		    break;
		case 18:
		    s = getContainer("GENERIC_9X2");
		    break;
		case 27:
		    s = getContainer("GENERIC_9X3");
		    break;
		case 36:
		    s = getContainer("GENERIC_9X4");
		    break;
		case 45:
		    s = getContainer("GENERIC_9X5");
		    break;
		case 54:
		    s = getContainer("GENERIC_9X6");
		    break;
		}

		Constructor<?> packet = PacketPlayOutOpenWindow.getConstructor(int.class, CraftContainers, getMinecraftClass("IChatBaseComponent"));
		Object newPack = packet.newInstance(getActiveContainerId(entityplayer), s, textToIChatBaseComponent("{\"text\": \"" + title + "\"}"));

		sendPlayerPacket(p, newPack);

		Field field = entityplayer.getClass().getField("activeContainer");
		Object container = CraftContainer.cast(field.get(entityplayer));

		Method meth = entityplayer.getClass().getMethod("updateInventory", CraftContainer);
		meth.invoke(entityplayer, container);
	    } else if (Version.isCurrentEqualOrHigher(Version.v1_8_R2)) {

		Object entityplayer = CraftPlayer.getMethod("getHandle").invoke(p);

		Constructor<?> packet = PacketPlayOutOpenWindow.getConstructor(int.class, String.class, getMinecraftClass("IChatBaseComponent"), int.class);
		Object newPack = packet.newInstance(getActiveContainerId(entityplayer), "minecraft:chest", textToIChatBaseComponent("{\"text\": \"" + title + "\"}"), p.getOpenInventory().getTopInventory()
		    .getSize());

		sendPlayerPacket(p, newPack);

		Field field = entityplayer.getClass().getField("activeContainer");
		Object container = CraftContainer.cast(field.get(entityplayer));

		Method meth = entityplayer.getClass().getMethod("updateInventory", CraftContainer);
		meth.invoke(entityplayer, container);

	    }
	} catch (Throwable e) {
	    e.printStackTrace();
	}
    }

    public static Object textToIChatBaseComponent(String text) {
	try {
	    Object serialized = nmsChatSerializer.getMethod("a", String.class).invoke(null, CMIChatColor.translate(text));
	    return serialized;
	} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
	    e.printStackTrace();
	}
	return text;
    }

    public static Object getPlayerHandle(Player player) {
	Object handle = null;
	try {
	    handle = player.getClass().getMethod("getHandle").invoke(player);
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return handle;
    }

    private static Object getPlayerConnection(Player player) {
	Object connection = null;
	try {
	    Object handle = getPlayerHandle(player);
	    connection = handle.getClass().getField("playerConnection").get(handle);
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return connection;
    }

    public static Class<?> getClass(String classname) {
	try {
	    String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
	    String path = classname.replace("{nms}", "net.minecraft.server." + version)
		.replace("{nm}", "net.minecraft." + version)
		.replace("{cb}", "org.bukkit.craftbukkit.." + version);
	    return Class.forName(path);
	} catch (Throwable t) {
	    t.printStackTrace();
	    return null;
	}
    }

    public static void sendPlayerPacket(Player player, Object packet) throws Exception {
	Object connection = getPlayerConnection(player);
	connection.getClass().getMethod("sendPacket", getClass("{nms}.Packet")).invoke(connection, packet);
    }

    private static Class<?> getBukkitClass(String nmsClassString) throws ClassNotFoundException {
	return Class.forName("org.bukkit.craftbukkit." + Version.getCurrent() + "." + nmsClassString);
    }

    public static Class<?> getMinecraftClass(String nmsClassString) throws ClassNotFoundException {
	return Class.forName("net.minecraft.server." + Version.getCurrent() + "." + nmsClassString);
    }

    public static ItemStack setNbt(ItemStack item, String path, String value) {
	if (item == null)
	    return null;
	try {
	    Object nmsStack = asNMSCopy(item);
	    if (nmsStack == null)
		return item;
	    Method methTag = nmsStack.getClass().getMethod("getTag");
	    Object tag = methTag.invoke(nmsStack);
	    if (tag == null)
		tag = NBTTagCompound.newInstance();
	    Method meth = tag.getClass().getMethod("setString", String.class, String.class);
	    meth.invoke(tag, path, value);
	    Method meth2 = nmsStack.getClass().getMethod("setTag", NBTTagCompound);
	    meth2.invoke(nmsStack, tag);
	    return (ItemStack) asBukkitCopy(nmsStack);
	} catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }

    public static String getItemMinecraftName(ItemStack item) {
	try {

	    Object nmsStack = asNMSCopy(item);

	    if (Version.isCurrentEqualOrHigher(Version.v1_13_R1)) {
		Object pre = nmsStack.getClass().getMethod("getItem").invoke(nmsStack);
		Object n = pre.getClass().getMethod("getName").invoke(pre);
		Class<?> ll = Class.forName("net.minecraft.server." + Version.getCurrent() + ".LocaleLanguage");
		Object lla = ll.getMethod("a").invoke(ll);
		return (String) lla.getClass().getMethod("a", String.class).invoke(lla, (String) n);
	    }

	    Field field = Item.getField("REGISTRY");
	    Object reg = field.get(field);
	    Method meth = reg.getClass().getMethod("b", Object.class);
	    meth.setAccessible(true);
	    Method secmeth = nmsStack.getClass().getMethod("getItem");
	    Object res2 = secmeth.invoke(nmsStack);
	    Object res = meth.invoke(reg, res2);
	    return res.toString();
	} catch (Exception e) {
	    return null;
	}
    }

    public static int getHoneyLevel(Block block) {
	if (CMIMaterial.get(block) != CMIMaterial.BEE_NEST && CMIMaterial.get(block) != CMIMaterial.BEEHIVE)
	    return 0;
	try {
	    Object nb = CraftBeehive.cast(block.getBlockData());
	    Method method = nb.getClass().getMethod("getHoneyLevel");
	    return (int) method.invoke(nb);
	} catch (Throwable e) {
	    e.printStackTrace();
	}
	return 0;
    }

    public static int getMaxHoneyLevel(Block block) {
	if (CMIMaterial.get(block) != CMIMaterial.BEE_NEST && CMIMaterial.get(block) != CMIMaterial.BEEHIVE)
	    return 0;
	try {
	    Object nb = CraftBeehive.cast(block.getBlockData());
	    Method method = nb.getClass().getMethod("getMaximumHoneyLevel");
	    return (int) method.invoke(nb);
	} catch (Throwable e) {
	    e.printStackTrace();
	}
	return 5;
    }

    public String getItemMinecraftNamePath(ItemStack item) {
	try {
	    Object nmsStack = asNMSCopy(item);
	    Method itemMeth = Item.getMethod("getById", int.class);
	    Object res = itemMeth.invoke(Item, item.getType().getId());
	    Method nameThingy = Item.getMethod("j", IStack);
	    Object resThingy = nameThingy.invoke(res, nmsStack);
	    return resThingy.toString();
	} catch (Exception e) {
	    return null;
	}
    }

    public static Object asNMSCopy(ItemStack item) {
	try {
	    Method meth = CraftItemStack.getMethod("asNMSCopy", ItemStack.class);
	    return meth.invoke(CraftItemStack, item);
	} catch (Exception e) {
	    return null;
	}
    }

    public static Object asBukkitCopy(Object item) {
	try {
	    Method meth = CraftItemStack.getMethod("asBukkitCopy", IStack);
	    return meth.invoke(CraftItemStack, item);
	} catch (Exception e) {
	    return null;
	}
    }

    public Object getCraftServer() {
	return CraftServer;
    }

    public static Object getNbt(ItemStack item, String path) {
	if (item == null)
	    return null;
	try {
	    Object nbt = getNbt(item);
	    if (nbt == null)
		return null;

	    Method meth = nbt.getClass().getMethod("getString", String.class);
	    Object res = meth.invoke(nbt, path);
	    return res;
	} catch (Throwable e) {
	    return null;
	}
    }

    public static Object getNbt(ItemStack item) {
	if (item == null)
	    return null;
	try {
	    Object nmsStack = asNMSCopy(item);
	    Method methTag = nmsStack.getClass().getMethod("getTag");
	    Object tag = methTag.invoke(nmsStack);
	    return tag;
	} catch (Throwable e) {
	    return null;
	}
    }

    public static ItemStack getItemInOffHand(Player player) {
	if (Version.getCurrent().isLower(Version.v1_9_R1))
	    return null;
	return player.getInventory().getItemInOffHand();
    }

    public void setEndermiteActive(Entity ent, boolean state) {

    }

    static Constructor<?> effectConstructor = null;
    static Method CraftParticleMethod = null;

    public static void playEffect(Player player, Location location, CMIEffect ef) {
	if (location == null || ef == null || location.getWorld() == null || player == null || !player.isOnline())
	    return;

	if (!location.getWorld().equals(player.getWorld()))
	    return;

	if (ef.getParticle() == null)
	    return;

	if (!ef.getParticle().isParticle())
	    return;

	try {

	    if (Version.isCurrentEqualOrLower(Version.v1_7_R4)) {

		Effect effect = ef.getParticle().getEffect();

		if (effect == null)
		    return;

		if (effectConstructor == null)
		    effectConstructor = PacketPlayOutWorldParticles.getConstructor(String.class, float.class, float.class, float.class, float.class, float.class, float.class, float.class, int.class);
		Object newPack = effectConstructor.newInstance(effect.name(), (float) location.getX(), (float) location.getY(), (float) location.getZ(), (float) ef.getOffset().getX(),
		    (float) ef.getOffset().getY(), (float) ef.getOffset().getZ(), ef.getSpeed(), ef.getAmount());
		sendPlayerPacket(player, newPack);
	    } else if (Version.isCurrentEqualOrLower(Version.v1_12_R1)) {

		Effect effect = ef.getParticle().getEffect();

		if (effect == null)
		    return;

		Object particle = ef.getParticle().getEnumParticle() == null ? null : EnumParticle.cast(ef.getParticle().getEnumParticle());
		int[] extra = ef.getParticle().getExtra();
		if (particle == null) {
		    for (Object p : EnumParticle.getEnumConstants()) {
			if (effect.name().replace("_", "").equalsIgnoreCase((p.toString().replace("_", ""))) ||
			    ef.getParticle().getName().replace("_", "").equalsIgnoreCase((p.toString().replace("_", ""))) ||
			    ef.getParticle().getSecondaryName().replace("_", "").equalsIgnoreCase((p.toString().replace("_", "")))) {
			    particle = p;
			    if (ef.getParticle().getEffect().getData() != null) {
				extra = new int[] { (0 << 12) | (0 & 0xFFF) };
			    }
			    break;
			}
		    }
		    if (extra == null) {
			extra = new int[0];
		    }
		}

		if (particle == null)
		    return;

		if (ef.getParticle().getEnumParticle() == null) {
		    ef.getParticle().setEnumParticle(particle);
		    ef.getParticle().setExtra(extra);
		}

		if (effectConstructor == null)
		    effectConstructor = PacketPlayOutWorldParticles.getConstructor(EnumParticle, boolean.class, float.class, float.class, float.class, float.class, float.class, float.class, float.class,
			int.class, int[].class);
		Object newPack = effectConstructor.newInstance(particle, true, (float) location.getX(), (float) location.getY(), (float) location.getZ(), (float) ef.getOffset().getX(),
		    (float) ef.getOffset().getY(),
		    (float) ef.getOffset().getZ(), ef.getSpeed(), ef.getAmount(), extra);

		sendPlayerPacket(player, newPack);
	    } else {

		org.bukkit.Particle particle = ef.getParticle().getParticle();

		if (particle == null)
		    return;

		org.bukkit.Particle.DustOptions dd = null;
		if (particle.equals(org.bukkit.Particle.REDSTONE))
		    dd = new org.bukkit.Particle.DustOptions(ef.getColor(), ef.getSize());

		if (CraftParticleMethod == null)
		    CraftParticleMethod = CraftParticle.getMethod("toNMS", org.bukkit.Particle.class, Object.class);

		if (effectConstructor == null)
		    effectConstructor = PacketPlayOutWorldParticles.getConstructor(ParticleParam, boolean.class, double.class, double.class, double.class, float.class, float.class, float.class,
			float.class, int.class);

		Object packet = effectConstructor.newInstance(CraftParticleMethod.invoke(null, particle, dd), true, location.getX(), location.getY(), location.getZ(), (float) ef
		    .getOffset().getX(), (float) ef.getOffset().getY(), (float) ef.getOffset().getZ(), ef.getSpeed(), ef.getAmount());
		sendPlayerPacket(player, packet);

	    }
	} catch (Throwable e) {
	    e.printStackTrace();
	}
    }
}
