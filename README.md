# Light's Autofish
> Download from [Modrinth](https://modrinth.com/mod/autofish-light)!

> This mod only provides Fabric support. Direct support of neither NeoForge nor Quilt is in consideration in this implementation.

Annoyed that you have to focus on when to reel in your fishing rod? Frustrated to find out that modded liquids and rods don't work with existing alternatives? Fret no longer, and give this mod a try!

Convenient configs are available in the mod menu or by pressing a hotkey (`v` by default) in-game. Modded liquids and fishing rods are explicitly supported, with full coverage listed below. Requests welcome!

Light's Autofish is a soft fork of [X+ Autofish](https://github.com/Wudji/XPlus-AutoFish), which in turn is an updated fork of [MrTroot's Autofish mod](https://www.curseforge.com/minecraft/mc-mods/autofish) for Minecraft 1.19.4+.

## Support coverage
### Mods
Crossed out entries indicate WIP status.

- [~~Fishing Frenzy~~](https://github.com/Vg34100/Minecraft-FishingFrenzy) (~~1.21.1~~) ([Modrinth](https://modrinth.com/mod/fishingfrenzy))
- [~~More Rod Variants~~](https://github.com/pnk2u/More-Fishing-Rod-Variants) (~~1.20.1~~, ~~1.21.1~~) ([Modrinth](https://modrinth.com/mod/more-fishing-rod-variants))
- [~~Nether Depths Upgrade~~](https://github.com/Scouter456/Nether_Depths_Upgrade) (~~1.20.1~~, ~~1.21.1~~) ([Modrinth](https://modrinth.com/mod/nether-depths-upgrade))
- [Spectrum](https://github.com/DaFuqs/Spectrum) (~~1.20.1~~, 1.21.1) ([Modrinth](https://modrinth.com/mod/spectrum))

### Versions
- 1.20.1 (WIP)
- 1.21.1
- 1.21.11 (WIP)

## FAQ
### Can I include this mod in my modpack?
Of course! Give proper credits and you should be fine.

### Will this mod support Minecraft version XXX?
Except for versions released in the last 3 months, only non-snapshot versions with major mod support on Modrinth are considered. The same rule applies to backporting to older versions, however no version prior to Minecraft version 1.18 will be supported.

### Modded content doesn't work in single player!
Due to mixin requirements, support for modded fishing rods and liquids are only available via multi-player detection. Please enforce multi-player detection whenever you can.

### Fishing rods constantly re-reel in the air!
It's caused by the very old code in persistent mode that disregards the past state, only snapshots in time captured every 10 seconds. I have a plan to fix this in the future with a refined algorithm, but until then please bear with it a bit longer. If you prefer the older algorithm, there will be a new toggle for the legacy behaviour.

### Can I request explicit mod support?
Sure! With caveats.

- The mod should **not** implement mechanics that largely deviates from vanilla fishing (e.g. [Fishing for Stars](https://modrinth.com/mod/forstars), [Steve Goes Fishing](https://modrinth.com/mod/steve-goes-fishing)).
- The mod has to fail with at least one of the two multi-player detection modes (bobber motion, bobber splash sound).
- If the mod is open-source or source available, **two** of the following two criteria must be met at the time of the request.
  - (**Required**) Enough downloads on Modrinth (35+ for the past week). This is to avoid implementing integrations that few people will use.
  - Pass checks on VirusTotal.
  - Pass manual audits from at least one of the maintainers.
- If the mod has zero source code available, **all** following criteria must be met.
  - Enough downloads on Modrinth (1050+ for the past week). 
  - Pass checks on VirusTotal.
  - Zero obfuscation. This mod avoids compile-time dependencies by using reflections, which requires unobfuscated class and method paths to work.
