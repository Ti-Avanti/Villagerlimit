package org.pvp.villagerlimit;

import net.jqwik.api.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.junit.jupiter.api.Tag;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

/**
 * 村民繁殖功能的属性测试
 * 使用 jqwik 进行基于属性的测试
 */
public class VillagerBreedingPropertyTest {
    
    /**
     * 属性 1：繁殖事件正确过滤
     * 
     * 对于任何 CreatureSpawnEvent 事件，当且仅当实体类型为 VILLAGER 且 SpawnReason 为 BREEDING 
     * 且配置允许繁殖时，系统才应该执行寿命设置逻辑。
     * 
     * 验证：需求 1.2, 7.1, 7.2, 7.3
     */
    @Property(tries = 100)
    @Tag("Feature: villager-breeding-lifespan, Property 1: 繁殖事件正确过滤")
    void breedingEventFiltering(
            @ForAll("entityTypes") EntityType entityType,
            @ForAll("spawnReasons") CreatureSpawnEvent.SpawnReason spawnReason,
            @ForAll boolean breedingAllowed,
            @ForAll boolean lifespanEnabled
    ) {
        // 创建模拟对象
        Villagerlimit plugin = mock(Villagerlimit.class);
        VillagerLimitConfig config = mock(VillagerLimitConfig.class);
        VillagerLifespanManager lifespanManager = mock(VillagerLifespanManager.class);
        CreatureSpawnEvent event = mock(CreatureSpawnEvent.class);
        Villager villager = mock(Villager.class);
        
        // 配置模拟对象
        when(plugin.getLimitConfig()).thenReturn(config);
        when(plugin.getLifespanManager()).thenReturn(lifespanManager);
        when(config.isBreedingAllowed()).thenReturn(breedingAllowed);
        when(config.isLifespanEnabled()).thenReturn(lifespanEnabled);
        when(config.isDebugEnabled()).thenReturn(false);
        when(config.getLifespanDays()).thenReturn(7);
        
        when(event.getEntityType()).thenReturn(entityType);
        when(event.getSpawnReason()).thenReturn(spawnReason);
        when(event.getEntity()).thenReturn(villager);
        
        // 创建监听器并触发事件
        VillagerSpawnListener listener = new VillagerSpawnListener(plugin);
        listener.onCreatureSpawn(event);
        
        // 验证：只有正确的组合才会调用寿命设置
        boolean shouldSetLifespan = 
            entityType == EntityType.VILLAGER &&
            spawnReason == CreatureSpawnEvent.SpawnReason.BREEDING &&
            breedingAllowed &&
            lifespanEnabled;
        
        if (shouldSetLifespan) {
            // 应该调用寿命设置方法
            verify(lifespanManager, times(1)).setVillagerLifespan(eq(villager), eq(7));
        } else {
            // 不应该调用寿命设置方法
            verify(lifespanManager, never()).setVillagerLifespan(any(), anyInt());
        }
        
        // 验证：只有在不允许繁殖且是繁殖事件时才取消事件
        if (entityType == EntityType.VILLAGER && 
            spawnReason == CreatureSpawnEvent.SpawnReason.BREEDING && 
            !breedingAllowed) {
            verify(event, times(1)).setCancelled(true);
        }
    }
    
    /**
     * 属性 6：早期返回优化
     * 
     * 对于任何非 VILLAGER 类型的 CreatureSpawnEvent，系统应该在不执行任何寿命相关逻辑的情况下立即返回；
     * 对于任何非 BREEDING 原因的村民生成，系统应该在不执行繁殖寿命逻辑的情况下返回。
     * 
     * 验证：需求 7.1, 7.2, 7.3
     */
    @Property(tries = 100)
    @Tag("Feature: villager-breeding-lifespan, Property 6: 早期返回优化")
    void earlyReturnOptimization(
            @ForAll("entityTypes") EntityType entityType,
            @ForAll("spawnReasons") CreatureSpawnEvent.SpawnReason spawnReason
    ) {
        // 创建模拟对象
        Villagerlimit plugin = mock(Villagerlimit.class);
        VillagerLimitConfig config = mock(VillagerLimitConfig.class);
        VillagerLifespanManager lifespanManager = mock(VillagerLifespanManager.class);
        CreatureSpawnEvent event = mock(CreatureSpawnEvent.class);
        
        // 配置模拟对象
        when(plugin.getLimitConfig()).thenReturn(config);
        when(plugin.getLifespanManager()).thenReturn(lifespanManager);
        when(config.isBreedingAllowed()).thenReturn(true);
        when(config.isLifespanEnabled()).thenReturn(true);
        when(config.isDebugEnabled()).thenReturn(false);
        
        when(event.getEntityType()).thenReturn(entityType);
        when(event.getSpawnReason()).thenReturn(spawnReason);
        
        // 创建监听器并触发事件
        VillagerSpawnListener listener = new VillagerSpawnListener(plugin);
        listener.onCreatureSpawn(event);
        
        // 验证：非目标情况下不执行寿命逻辑
        boolean shouldProcess = 
            entityType == EntityType.VILLAGER &&
            spawnReason == CreatureSpawnEvent.SpawnReason.BREEDING;
        
        if (!shouldProcess) {
            // 不应该调用寿命管理器
            verify(lifespanManager, never()).setVillagerLifespan(any(), anyInt());
            // 不应该访问配置的繁殖和寿命设置（除了可能的 isBlockNaturalSpawn）
            verify(config, never()).isBreedingAllowed();
            verify(config, never()).isLifespanEnabled();
        }
    }
    
    // 提供实体类型的生成器
    @Provide
    Arbitrary<EntityType> entityTypes() {
        return Arbitraries.of(
            EntityType.VILLAGER,
            EntityType.ZOMBIE,
            EntityType.CREEPER,
            EntityType.COW,
            EntityType.PIG,
            EntityType.SHEEP
        );
    }
    
    // 提供生成原因的生成器
    @Provide
    Arbitrary<CreatureSpawnEvent.SpawnReason> spawnReasons() {
        return Arbitraries.of(
            CreatureSpawnEvent.SpawnReason.BREEDING,
            CreatureSpawnEvent.SpawnReason.NATURAL,
            CreatureSpawnEvent.SpawnReason.SPAWNER_EGG,
            CreatureSpawnEvent.SpawnReason.SPAWNER,
            CreatureSpawnEvent.SpawnReason.CURED,
            CreatureSpawnEvent.SpawnReason.CUSTOM
        );
    }
}
