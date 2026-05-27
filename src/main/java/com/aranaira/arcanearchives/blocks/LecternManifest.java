package com.aranaira.arcanearchives.blocks;

import com.aranaira.arcanearchives.AAGuiHandler;
import com.aranaira.arcanearchives.ArcaneArchives;
import com.aranaira.arcanearchives.blocks.templates.BlockDirectionalTemplate;
import com.aranaira.arcanearchives.data.types.ClientNetwork;
import com.aranaira.arcanearchives.data.DataHelper;
import com.aranaira.arcanearchives.init.ItemRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.List;

public class LecternManifest extends BlockDirectionalTemplate {

	public static final String name = "lectern_manifest";
	public static final PropertyBool HAS_MANIFEST = PropertyBool.create("has_manifest");

	public LecternManifest () {
		super(name, Material.WOOD);
		setHarvestLevel("axe", 0);
		this.setHardness(1.5f);
		setSize(1, 2, 1);
		setLightLevel(16f / 16f);
		this.setDefaultState(this.getDefaultState().withProperty(ACCESSOR, false).withProperty(HAS_MANIFEST, true));
	}

	@Override
	public boolean hasOBJModel () {
		return true;
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean isOpaqueCube (IBlockState state) {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation (ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		tooltip.add(TextFormatting.GOLD + I18n.format("arcanearchives.tooltip.device.lectern_manifest"));
	}

	@Override
	public void getDrops (@Nonnull NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, @Nonnull IBlockState state, int fortune) {
		super.getDrops(drops, world, pos, state, fortune);
		if (state.getValue(HAS_MANIFEST)) {
			drops.add(new ItemStack(ItemRegistry.MANIFEST));
		}
	}

	@Override
	public boolean onBlockActivated (World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (state.getValue(ACCESSOR)) {
			pos = pos.down();
			state = worldIn.getBlockState(pos);
		}

		boolean hasManifest = state.getValue(HAS_MANIFEST);
		ItemStack heldItem = playerIn.getHeldItem(hand);

		if (playerIn.isSneaking() && hasManifest && heldItem.isEmpty()) {
			if (!worldIn.isRemote) {
				worldIn.setBlockState(pos, state.withProperty(HAS_MANIFEST, false));
				ItemStack manifestStack = new ItemStack(ItemRegistry.MANIFEST);
				if (!playerIn.inventory.addItemStackToInventory(manifestStack)) {
					playerIn.dropItem(manifestStack, false);
				}
				worldIn.playSound(null, pos, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1.0F, 1.0F);
			}
			return true;
		}

		if (hasManifest && worldIn.isRemote) {
			ClientNetwork network = DataHelper.getClientNetwork(playerIn.getUniqueID());
			network.manifestItems.clear();
			network.synchroniseManifest();
			playerIn.openGui(ArcaneArchives.instance, AAGuiHandler.MANIFEST, worldIn, pos.getX(), pos.getY(), pos.getZ());
		}

		return hasManifest;
	}

	@Override
	@SuppressWarnings("deprecation")
	public IBlockState getStateFromMeta (int meta) {
		boolean hasManifest = (meta & 2) != 0;
		boolean accessor = (meta & 1) != 0;
		int facingIndex = (meta >> 2) & 3;
		EnumFacing facing;
		switch (facingIndex) {
			case 0: facing = EnumFacing.NORTH; break;
			case 1: facing = EnumFacing.SOUTH; break;
			case 2: facing = EnumFacing.WEST; break;
			default: facing = EnumFacing.EAST; break;
		}
		return getDefaultState().withProperty(getFacingProperty(), facing).withProperty(ACCESSOR, accessor).withProperty(HAS_MANIFEST, hasManifest);
	}

	@Override
	public int getMetaFromState (IBlockState state) {
		int facingIndex;
		switch (state.getValue(getFacingProperty())) {
			case NORTH: facingIndex = 0; break;
			case SOUTH: facingIndex = 1; break;
			case WEST: facingIndex = 2; break;
			default: facingIndex = 3; break;
		}
		return (facingIndex << 2) | (state.getValue(ACCESSOR) ? 1 : 0) | (state.getValue(HAS_MANIFEST) ? 2 : 0);
	}

	@Override
	public BlockRenderLayer getRenderLayer () {
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	@SuppressWarnings("deprecation")
	public void neighborChanged (IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
		if (state.getValue(ACCESSOR)) {
			if (world.isAirBlock(pos.down())) {
				world.setBlockToAir(pos);
			}
		} else {
			if (world.isAirBlock(pos.up())) {
				// TODO: PARTICLES
				world.setBlockToAir(pos);
			}
		}

		super.neighborChanged(state, world, pos, blockIn, fromPos);
	}

	@Override
	protected BlockStateContainer createBlockState () {
		return new BlockStateContainer(this, getFacingProperty(), ACCESSOR, HAS_MANIFEST);
	}

}
