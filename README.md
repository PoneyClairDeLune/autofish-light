# Light's Autofish
> Download from [Modrinth](https://modrinth.com/mod/autofish-light)!

> This mod is currently going through a rewrite to reduce technical debt.

> This mod only supports Fabric. Direct support for NeoForge or Quilt is not planned.

Annoyed that you have to focus on when to reel in your fishing rod? Frustrated to find out that modded liquids and rods don't work with existing alternatives? Fret no longer, and give this mod a try!

Convenient configs are available in the mod menu or by pressing a hotkey (`v` by default) in-game. Modded liquids and fishing rods are explicitly supported, with full coverage listed below. Requests welcome!

Light's Autofish is a hard fork of [X+ Autofish](https://github.com/Wudji/XPlus-AutoFish), which in turn is an updated fork of [MrTroot's Autofish mod](https://www.curseforge.com/minecraft/mc-mods/autofish) for Minecraft 1.19.4+.

![use-fabric](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/supported/fabric_64h.png)![no-forge](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/unsupported/forge_64h.png)![no-quilt](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/unsupported/quilt_64h.png)

![dep-fabric-api](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/requires/fabric-api_64h.png)![dep-cloth-config-api](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/requires/cloth-config-api_64h.png)

[![dist-github](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/github_64h.png)](https://github.com/PoneyClairDeLune/autofish-light)[![dist-codeberg](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/codeberg_64h.png)](https://codeberg.org/PoneyClairDeLune/autofish-light)

## Support coverage
### Mods
Crossed out entries indicate WIP status. Content of some mods might only be offered via [Polymer](https://modrinth.com/mod/polymer), which are marked in italic in the relevant versions.

- [~~Fishing Frenzy~~](https://github.com/Vg34100/Minecraft-FishingFrenzy) (~~1.21.1~~) ([Modrinth](https://modrinth.com/mod/fishingfrenzy))
- [Go Fish](https://github.com/Draylar/go-fish) (~~1.20.1~~, 1.21.1, _26.1.x_) ([CurseForge](https://www.curseforge.com/minecraft/mc-mods/go-fish), [CurseForge](https://www.curseforge.com/minecraft/mc-mods/go-fish-updated))
- [~~More Rod Variants~~](https://github.com/pnk2u/More-Fishing-Rod-Variants) (~~1.20.1~~, 1.21.1, 1.21.11, 26.1.x) ([Modrinth](https://modrinth.com/mod/more-fishing-rod-variants))
- [~~Nether Depths Upgrade~~](https://github.com/Scouter456/Nether_Depths_Upgrade) (~~1.20.1~~, ~~1.21.1~~) ([Modrinth](https://modrinth.com/mod/nether-depths-upgrade))
- [Spectrum](https://github.com/DaFuqs/Spectrum) (~~1.20.1~~, 1.21.1) ([Modrinth](https://modrinth.com/mod/spectrum))

### Versions
Crossed out entries indicate WIP status.

- ~~1.20.1~~ (`fabric-1.20.1`) [LTS]
- 1.21.1 (`fabric-1.21.1`) [LTS]
- 1.21.11 (`fabric-1.21.11`) [LTS]
- 26.1.x (`fabric-1.22.1`)
- 26.2.x (`fabric-1.23.1`)

## FAQ
### Can I include this mod in my mod pack?
Of course! Give proper credits and you should be fine.

### Will this mod support Minecraft version XXX?
New versions of Minecraft will be supported with best effort, and old versions with major mod support on Modrinth have LTS support considered. The same rule applies to backporting to older versions, however no version prior to Minecraft version 1.18 will be supported.

The built versions of the mod may already work on other versions, although it's neither tested nor guaranteed.

### Modded content doesn't work in single player!
> This mod has removed singleplayer-specific detection, making this FAQ entry obsolete. Mod compatibility problem should refer to the "request mod support" section below.

Due to mixin requirements, support for modded fishing rods and liquids is only available via multiplayer detection. Please enforce multiplayer detection whenever you can.

### Fishing rods constantly re-reel in the air!
It's caused by the very old code in persistent mode that disregards the past state, only snapshots in time captured every 10 seconds. I have a plan to fix this in the future with a refined algorithm, but until then please bear with it a bit longer. If you prefer the older algorithm, there will be a new toggle for the legacy behaviour.

### Can I request explicit mod support?
Sure! With caveats.

- The mod should **not** implement mechanics that largely deviates from vanilla fishing (e.g. [Fishing for Stars](https://modrinth.com/mod/forstars), [Steve Goes Fishing](https://modrinth.com/mod/steve-goes-fishing)).
- The mod has to fail with at least one of the two multiplayer detection modes (bobber motion, bobber splash sound).
- If the mod is open-source or source available, **two** of the following criteria must be met at the time of the request.
  - (**Required**) Enough downloads on Modrinth (35+ for the past week). This is to avoid implementing integrations that few people will use.
  - Pass checks on VirusTotal.
  - Pass manual audits from at least one of the maintainers.
- If the mod has zero source code available, **all** following criteria must be met.
  - Enough downloads on Modrinth (1050+ for the past week). 
  - Pass checks on VirusTotal.
  - Zero obfuscation. This mod avoids compile-time dependencies by using reflections, which requires unobfuscated class and method paths to work.

## Technical details
### Constraints
- Touch mixins as little as possible, and avoid them whenever possible.
