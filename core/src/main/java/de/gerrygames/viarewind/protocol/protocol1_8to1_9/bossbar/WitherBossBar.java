package de.gerrygames.viarewind.protocol.protocol1_8to1_9.bossbar;

import de.gerrygames.viarewind.protocol.protocol1_8to1_9.Protocol1_8TO1_9;
import de.gerrygames.viarewind.utils.PacketUtil;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.boss.BossBar;
import us.myles.ViaVersion.api.boss.BossColor;
import us.myles.ViaVersion.api.boss.BossFlag;
import us.myles.ViaVersion.api.boss.BossStyle;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import us.myles.ViaVersion.api.minecraft.metadata.types.MetaType1_8;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.types.version.Types1_8;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class WitherBossBar extends BossBar<UserConnection> {
	private static int highestId = Integer.MAX_VALUE-10000;

	private final UUID uuid;
	private String title;
	private float health;
	private boolean visible = false;

	private final UserConnection connection;

	private final int entityId = highestId++;
	private double locX, locY, locZ;

	public WitherBossBar(UserConnection connection, UUID uuid, String title, float health) {
		this.connection = connection;
		this.uuid = uuid;
		this.title = title;
		this.health = health;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public WitherBossBar setTitle(String title) {
		this.title = title;
		if (this.visible) updateMetadata();
		return this;
	}

	@Override
	public float getHealth() {
		return health;
	}

	@Override
	public WitherBossBar setHealth(float health) {
		this.health  = health;
		if (this.health<=0) this.health = 0.0001f;
		if (this.visible) updateMetadata();
		return this;
	}

	@Override
	public BossColor getColor() {
		return null;
	}

	@Override
	public WitherBossBar setColor(BossColor bossColor) {
		throw new UnsupportedOperationException(this.getClass().getName() + " does not support color");
	}

	@Override
	public BossStyle getStyle() {
		return null;
	}

	@Override
	public WitherBossBar setStyle(BossStyle bossStyle) {
		throw new UnsupportedOperationException(this.getClass().getName() + " does not support styles");
	}

	@Override
	public WitherBossBar addPlayer(UUID uuid) {
		throw new UnsupportedOperationException(this.getClass().getName() + " is only for one UserConnection!");
	}

    @Override
    public WitherBossBar addConnection(UserConnection userConnection) {
        throw new UnsupportedOperationException(this.getClass().getName() + " is only for one UserConnection!");
    }

    @Override
	public WitherBossBar removePlayer(UUID uuid) {
		throw new UnsupportedOperationException(this.getClass().getName() + " is only for one UserConnection!");
	}

    @Override
    public WitherBossBar removeConnection(UserConnection userConnection) {
        throw new UnsupportedOperationException(this.getClass().getName() + " is only for one UserConnection!");
    }

    @Override
	public WitherBossBar addFlag(BossFlag bossFlag) {
		throw new UnsupportedOperationException(this.getClass().getName() + " does not support flags");
	}

	@Override
	public WitherBossBar removeFlag(BossFlag bossFlag) {
		throw new UnsupportedOperationException(this.getClass().getName() + " does not support flags");
	}

	@Override
	public boolean hasFlag(BossFlag bossFlag) {
		return false;
	}

	@Override
	public Set<UUID> getPlayers() {
		return Collections.singleton(connection.get(ProtocolInfo.class).getUuid());
	}

    @Override
    public Set<UserConnection> getConnections() {
        throw new UnsupportedOperationException(this.getClass().getName() + " is only for one UserConnection!");
    }

    @Override
	public WitherBossBar show() {
		if (!this.visible) {
			this.visible = true;
			spawnWither();
		}
		return this;
	}

	@Override
	public WitherBossBar hide() {
		if (this.visible) {
			this.visible = false;
			despawnWither();
		}
		return this;
	}

	@Override
	public boolean isVisible() {
		return visible;
	}

	@Override
	public UUID getId() {
		return this.uuid;
	}

	public void setLocation(double x, double y, double z) {
		locX = x;
		locY = y;
		locZ = z;
		updateLocation();
	}

	private void spawnWither() {
		PacketWrapper wrapper = new PacketWrapper(0x0F, null, this.connection);
		wrapper.write(Type.VAR_INT, entityId);
		wrapper.write(Type.UNSIGNED_BYTE, (short)64);
		wrapper.write(Type.INT, (int) (locX * 32d));
		wrapper.write(Type.INT, (int) (locY * 32d));
		wrapper.write(Type.INT, (int) (locZ * 32d));
		wrapper.write(Type.BYTE, (byte)0);
		wrapper.write(Type.BYTE, (byte)0);
		wrapper.write(Type.BYTE, (byte)0);
		wrapper.write(Type.SHORT, (short)0);
		wrapper.write(Type.SHORT, (short)0);
		wrapper.write(Type.SHORT, (short)0);

		List<Metadata> metadata = new ArrayList<>();
		metadata.add(new Metadata(0, MetaType1_8.Byte, (byte) 0x20));
		metadata.add(new Metadata(2, MetaType1_8.String, title));
		metadata.add(new Metadata(3, MetaType1_8.Byte, (byte) 1));
		metadata.add(new Metadata(6, MetaType1_8.Float, health * 300f));

		wrapper.write(Types1_8.METADATA_LIST, metadata);

		PacketUtil.sendPacket(wrapper, Protocol1_8TO1_9.class, true, true);
	}

	private void updateLocation() {
		PacketWrapper wrapper = new PacketWrapper(0x18, null, this.connection);
		wrapper.write(Type.VAR_INT, entityId);
		wrapper.write(Type.INT, (int) (locX * 32d));
		wrapper.write(Type.INT, (int) (locY * 32d));
		wrapper.write(Type.INT, (int) (locZ * 32d));
		wrapper.write(Type.BYTE, (byte)0);
		wrapper.write(Type.BYTE, (byte)0);
		wrapper.write(Type.BOOLEAN, false);

		PacketUtil.sendPacket(wrapper, Protocol1_8TO1_9.class, true, true);
	}

	private void updateMetadata() {
		PacketWrapper wrapper = new PacketWrapper(0x1C, null, this.connection);
		wrapper.write(Type.VAR_INT, entityId);

		List<Metadata> metadata = new ArrayList<>();
		metadata.add(new Metadata(2, MetaType1_8.String, title));
		metadata.add(new Metadata(6, MetaType1_8.Float, health * 300f));

		wrapper.write(Types1_8.METADATA_LIST, metadata);

		PacketUtil.sendPacket(wrapper, Protocol1_8TO1_9.class, true, true);
	}

	private void despawnWither() {
		PacketWrapper wrapper = new PacketWrapper(0x13, null, this.connection);
		wrapper.write(Type.VAR_INT_ARRAY_PRIMITIVE, new int[] {entityId});

		PacketUtil.sendPacket(wrapper, Protocol1_8TO1_9.class, true, true);
	}

	public void setPlayerLocation(double posX, double posY, double posZ, float yaw, float pitch) {
		double yawR = Math.toRadians(yaw);
		double pitchR = Math.toRadians(pitch);

		posX -= Math.cos(pitchR) * Math.sin(yawR) * 48;
		posY -= Math.sin(pitchR) * 48;
		posZ += Math.cos(pitchR) * Math.cos(yawR) * 48;

		setLocation(posX, posY, posZ);
	}
}
