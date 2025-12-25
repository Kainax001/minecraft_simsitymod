package com.kainax00.simcitymod.entity;

import com.kainax00.simcitymod.config.TradeConfig;
import com.kainax00.simcitymod.config.TradeConfig.TradeRecipe;
import com.kainax00.simcitymod.util.IdentifierUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalInt;

public class TraderEntity extends PathfinderMob implements Merchant, InventoryCarrier {

    @Nullable
    private Player tradingPlayer;
    private MerchantOffers offers;
    private final SimpleContainer inventory = new SimpleContainer(8);

    public TraderEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setInvulnerable(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.JUMP_STRENGTH, 0.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide() && hand == InteractionHand.MAIN_HAND) {
            this.setTradingPlayer(player);
            this.openTradingScreen(player, this.getDisplayName(), 1);
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    // --- [Merchant interface] ---

    @Override
    public void setTradingPlayer(@Nullable Player player) {
        this.tradingPlayer = player;
    }

    @Nullable
    @Override
    public Player getTradingPlayer() {
        return this.tradingPlayer;
    }

    @Override
    public MerchantOffers getOffers() {
        if (this.offers == null) {
            this.offers = new MerchantOffers();
            updateTrades();
        }
        return this.offers;
    }

    private void updateTrades() {
        for (TradeRecipe recipe : TradeConfig.getTrades()) {
            Item inputItem = ForgeRegistries.ITEMS.getValue(IdentifierUtil.parse(recipe.inputItem));
            Item outputItem = ForgeRegistries.ITEMS.getValue(IdentifierUtil.parse(recipe.outputItem));

            if (inputItem != null && outputItem != null) {
                MerchantOffer offer = new MerchantOffer(
                    new ItemCost(inputItem, recipe.inputCount),
                    new ItemStack(outputItem, recipe.outputCount),
                    999,
                    0,
                    0.00f
                );
                this.offers.add(offer);
            }
        }
    }

    @Override
    public void overrideOffers(@Nullable MerchantOffers offers) {
        this.offers = offers;
    }

    @Override
    public void notifyTrade(MerchantOffer offer) {
        offer.increaseUses();
        this.playSound(SoundEvents.VILLAGER_YES, 1.0F, 1.0F);
    }

    @Override
    public void notifyTradeUpdated(ItemStack stack) {}

    @Override
    public int getVillagerXp() { return 0; }

    @Override
    public void overrideXp(int xp) {}

    @Override
    public boolean showProgressBar() { return false; }

    @Override
    public SoundEvent getNotifyTradeSound() { return SoundEvents.VILLAGER_YES; }

    @Override
    public boolean isClientSide() {
        return this.level().isClientSide();
    }

    @Override
    public boolean stillValid(Player player) {
        return this.getTradingPlayer() == player && this.distanceToSqr(player) < 64.0D;
    }

    @Override
    public void openTradingScreen(Player player, Component displayName, int level) {
        OptionalInt containerId = player.openMenu(new SimpleMenuProvider((id, inventory, p) -> {
            return new MerchantMenu(id, inventory, this);
        }, displayName));
        
        if (containerId.isPresent()) {
            MerchantOffers offers = this.getOffers();
            if (!offers.isEmpty()) {
                player.sendMerchantOffers(containerId.getAsInt(), offers, level, this.getVillagerXp(), this.showProgressBar(), this.canRestock());
            }
        }
    }

    @Override
    public boolean canRestock() { return true; }

    @Override
    public SimpleContainer m_214176_() {
        return this.inventory;
    }
}