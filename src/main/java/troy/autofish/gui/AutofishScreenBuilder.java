package troy.autofish.gui;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import troy.autofish.FabricModAutofish;
import troy.autofish.config.Config;

import java.util.function.Function;

public class AutofishScreenBuilder {
	private static final Function<Boolean, Component> booleanTextComponent = bool -> {
		return Component.translatable(bool ? "options.autofish.toggle.on" : "options.autofish.toggle.off");
	};

	public static Screen buildScreen(FabricModAutofish modAutofish, Screen parentScreen) {
		Config defaults = new Config();
		Config config = modAutofish.getConfig();

		ConfigBuilder builder = ConfigBuilder.create()
		.setParentScreen(parentScreen)
		.setTitle(
			Component.translatable("options.autofish.title")
		).transparentBackground()
		.setDoesConfirmSave(true)
		.setSavingRunnable(() -> {
			modAutofish.getConfig().enforceConstraints();
			modAutofish.getConfigManager().writeConfig(true);
		});

		ConfigEntryBuilder entryBuilder = builder.entryBuilder();
		ConfigCategory configCat = builder.getOrCreateCategory(
			Component.translatable("options.autofish.config")
		);

		// If the mod should be enabled.
		AbstractConfigListEntry<Boolean> toggleAutofish = entryBuilder.startBooleanToggle(
			Component.translatable("options.autofish.enable.title"),
			config.modEnabled()
		).setDefaultValue(
			defaults.modEnabled()
		).setTooltip(
			Component.translatable("options.autofish.enable.tooltip")
		).setSaveConsumer(value -> {
			modAutofish.getConfig().modEnabled(value);
		}).setYesNoTextSupplier(
			booleanTextComponent
		).build();

		// Should the mod use multiple rods.
		AbstractConfigListEntry<Boolean> toggleMultiRod = entryBuilder.startBooleanToggle(
			Component.translatable("options.autofish.multirod.title"),
			config.multiRod()
		).setDefaultValue(
			defaults.multiRod()
		).setTooltip(
			Component.translatable("options.autofish.multirod.tooltip")
		).setSaveConsumer(newValue -> {
			modAutofish.getConfig().multiRod(newValue);
		}).setYesNoTextSupplier(
			booleanTextComponent
		).build();

		// Should the mod detect open water.
		AbstractConfigListEntry<Boolean> toggleOpenWaterDetection = entryBuilder.startBooleanToggle(
			Component.translatable("options.autofish.open_water_detection.title"),
			config.openWaterDetected()
		).setDefaultValue(
			defaults.openWaterDetected()
		).setTooltip(
			Component.translatable("options.autofish.open_water_detection.tooltip")
		).setSaveConsumer(newValue -> {
			modAutofish.getConfig().openWaterDetected(newValue);
		}).setYesNoTextSupplier(
			booleanTextComponent
		).build();

		// Should the mod prevent rods from breaking.
		AbstractConfigListEntry<Boolean> toggleBreakProtection = entryBuilder.startBooleanToggle(
			Component.translatable("options.autofish.break_protection.title"),
			config.rodBreakAvoided()
		).setDefaultValue(
			defaults.rodBreakAvoided()
		).setTooltip(
			Component.translatable("options.autofish.break_protection.tooltip")
		).setSaveConsumer(newValue -> {
			modAutofish.getConfig().rodBreakAvoided(newValue);
		}).setYesNoTextSupplier(
			booleanTextComponent
		).build();

		// Should the mod attempt recasts.
		AbstractConfigListEntry<Boolean> togglePersistentMode = entryBuilder.startBooleanToggle(
			Component.translatable("options.autofish.persistent.title"),
			config.persistentMode()
		).setDefaultValue(defaults.persistentMode()
		).setTooltip(
			Component.translatable("options.autofish.persistent.tooltip")
		).setSaveConsumer(newValue -> {
			modAutofish.getConfig().persistentMode(newValue);
		}).setYesNoTextSupplier(
			booleanTextComponent
		).build();


		// Should the mod use sound-based detection.
		AbstractConfigListEntry<Boolean> toggleSoundDetection = entryBuilder.startBooleanToggle(
			Component.translatable("options.autofish.sound.title"),
			config.soundUsed()
		).setDefaultValue(
			defaults.soundUsed()
		).setTooltip(
			Component.translatable("options.autofish.sound.tooltip")
		).setSaveConsumer(newValue -> {
			modAutofish.getConfig().soundUsed(newValue);
			modAutofish.getAutofish().setDetection();
		}).setYesNoTextSupplier(
			booleanTextComponent
		).build();

		// Should the open water detection algorithm use the new algorithm.
		AbstractConfigListEntry<Boolean> toggleOpenWaterNewAlgo = entryBuilder.startBooleanToggle(
			Component.translatable("options.autofish.open_water_new_algorithm.title"),
			config.openWaterNewAlgo()
		).setDefaultValue(
			defaults.openWaterNewAlgo()
		).setTooltip(
			Component.translatable("options.autofish.open_water_new_algorithm.tooltip")
		).setSaveConsumer(value -> {
			modAutofish.getConfig().openWaterNewAlgo(value);
		}).setYesNoTextSupplier(
			booleanTextComponent
		).build();

		// Should the detection results be reported constantly.
		AbstractConfigListEntry<Boolean> toggleNoisyDetection = entryBuilder.startBooleanToggle(
			Component.translatable("options.autofish.noisy_detection.title"),
			config.noisyDetection()
		).setDefaultValue(
			defaults.noisyDetection()
		).setTooltip(
			Component.translatable("options.autofish.noisy_detection.tooltip")
		).setSaveConsumer(value -> {
			modAutofish.getConfig().noisyDetection(value);
		}).setYesNoTextSupplier(
			booleanTextComponent
		).build();

		// Should the persistent mode use the legacy implementation.
		AbstractConfigListEntry<Boolean> toggleLegacyPersistence = entryBuilder.startBooleanToggle(
			Component.translatable("options.autofish.legacy_persistence.title"),
			config.legacyPersistence()
		).setDefaultValue(
			defaults.legacyPersistence()
		).setTooltip(
			Component.translatable("options.autofish.legacy_persistence.tooltip")
		).setSaveConsumer(value -> {
			modAutofish.getConfig().legacyPersistence(value);
		}).setYesNoTextSupplier(
			booleanTextComponent
		).build();

		// Configure the remaining durability a rod should target.
		AbstractConfigListEntry<Integer> sliderDamageSafeMargin = entryBuilder.startIntSlider(
			Component.translatable("options.autofish.damage_safe_margin.title"),
			config.damageSafeMargin(),
			1, 32
		).setDefaultValue(
			defaults.damageSafeMargin()
		).setTooltip(
			Component.translatable("options.autofish.damage_safe_margin.tooltip")
		).setSaveConsumer(value -> {
			modAutofish.getConfig().damageSafeMargin(value);
		}).setTextGetter(
			value -> Component.translatable("options.autofish.damage_safe_margin.value", value)
		).build();

		// Configure the delay between recasts.
		AbstractConfigListEntry<Long> recastDelaySlider = entryBuilder.startLongSlider(Component.translatable("options.autofish.recast_delay.title"), config.getRecastDelay(), 500, 5000)
			.setDefaultValue(defaults.getRecastDelay())
			.setTooltip(
				Component.translatable("options.autofish.recast_delay.tooltip_0"),
				Component.translatable("options.autofish.recast_delay.tooltip_1")
			)
			.setTextGetter(value -> Component.translatable("options.autofish.recast_delay.value", value))
			.setSaveConsumer(newValue -> {
				modAutofish.getConfig().setRecastDelay(newValue);
			})
			.build();

		// Configure the maximum delta between randomised recasts.
		AbstractConfigListEntry<Long> randomDelaySlider = entryBuilder.startLongSlider(Component.translatable("options.autofish.random_delay.title"), config.getRandomDelay(), 0, 75)
			.setDefaultValue(defaults.getRandomPercent())
			.setTooltip(
				Component.translatable("options.autofish.random_delay.tooltip_0"),
				Component.translatable("options.autofish.random_delay.tooltip_1"),
				Component.translatable("options.autofish.random_delay.tooltip_2"),
				Component.translatable("options.autofish.random_delay.tooltip_3")
			)
			.setTextGetter(value -> Component.translatable("options.autofish.random_delay.value", value))
			.setSaveConsumer(newValue -> {
				modAutofish.getConfig().setRandomDelay(newValue);
			})
			.build();

		// Configure a delay upon reeling in the rod.
		AbstractConfigListEntry<Long> reelInDelay = entryBuilder.startLongSlider(Component.translatable("options.autofish.reel_in_delay.title"), config.getReelInDelay(), 1, 2000)
			.setDefaultValue(defaults.getReelInDelay())
			.setTooltip(
				Component.translatable("options.autofish.reel_in_delay.tooltip_0"),
				Component.translatable("options.autofish.reel_in_delay.tooltip_1")
			)
			.setTextGetter(value -> Component.translatable("options.autofish.reel_in_delay.value", value))
			.setSaveConsumer(newValue -> {
				modAutofish.getConfig().setReelInDelay(newValue);
			})
			.build();

		// RegEx pattern for ClearLag.
		AbstractConfigListEntry<String> clearLagRegexField = entryBuilder.startTextField(Component.translatable("options.autofish.clear_regex.title"), config.getClearLagRegex())
			.setDefaultValue(defaults.getClearLagRegex())
			.setTooltip(
				Component.translatable("options.autofish.clear_regex.tooltip_0"),
				Component.translatable("options.autofish.clear_regex.tooltip_1"),
				Component.translatable("options.autofish.clear_regex.tooltip_2")
			)
			.setSaveConsumer(newValue -> {
				modAutofish.getConfig().setClearLagRegex(newValue);
			})
			.build();

		SubCategoryBuilder subCatBuilderBasic = entryBuilder.startSubCategory(Component.translatable("options.autofish.basic.title"));
		subCatBuilderBasic.add(toggleAutofish);
		subCatBuilderBasic.add(toggleMultiRod);
		subCatBuilderBasic.add(toggleOpenWaterDetection);
		subCatBuilderBasic.add(toggleBreakProtection);
		subCatBuilderBasic.add(togglePersistentMode);
		subCatBuilderBasic.setExpanded(true);
		configCat.addEntry(subCatBuilderBasic.build());

		SubCategoryBuilder subCatBuilderAdvanced = entryBuilder.startSubCategory(Component.translatable("options.autofish.advanced.title"));
		subCatBuilderAdvanced.add(toggleSoundDetection);
		subCatBuilderAdvanced.add(toggleOpenWaterNewAlgo);
		subCatBuilderAdvanced.add(toggleNoisyDetection);
		subCatBuilderAdvanced.add(toggleLegacyPersistence);
		subCatBuilderAdvanced.add(sliderDamageSafeMargin);
		subCatBuilderAdvanced.add(recastDelaySlider);
		subCatBuilderAdvanced.add(randomDelaySlider);
		subCatBuilderAdvanced.add(reelInDelay);
		subCatBuilderAdvanced.add(clearLagRegexField);
		subCatBuilderAdvanced.setExpanded(true);
		configCat.addEntry(subCatBuilderAdvanced.build());

		return builder.build();
	}
}
