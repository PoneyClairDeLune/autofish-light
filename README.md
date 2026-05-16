# Light's Autofish
> Download from [Modrinth](https://modrinth.com/mod/autofish-light)!

> This mod only provides Fabric support. Direct support of neither NeoForge nor Quilt is in consideration in this implementation.

Frustrated that you have to focus on when to reel in your fishing rod? Stunned to find out that modded liquid and rods don't work with existing alternatives? Give this mod a try!

Convenient configs are available in the mod menu or by pressing a hotkey (default is `v`) in-game. Explicitly supports modded rods and liquid, like those in [Spectrum](https://github.com/DaFuqs/Spectrum), with full coverage listed below.

Light's Autofish is a soft fork of [X+ Autofish](https://github.com/Wudji/XPlus-AutoFish), which in turn is an updated fork of [MrTroot's Autofish mod](https://www.curseforge.com/minecraft/mc-mods/autofish) for Minecraft 1.19.4+.

## Support coverage
### Mods
- Spectrum

### Versions
- 1.20.1 (WIP)
- 1.21.1

## FAQ
### Modded content doesn't work in single player!
Support for modded fishing rods and liquids are only available with multi-player detection methods. Please enforce multi-player detection if you can.

### Fishing rods constantly re-reel in the air!
It's caused by the very old code in persistent mode that disregards the past state, only snapshots in time captured every 10 seconds. I have a plan to fix this in the future with a refined algorithm, but until then please bear with it a bit longer. If you prefer the older algorithm, there will be a new toggle for the legacy behaviour.

### Can I request explicit mod support?
Sure! With caveats.

- The mod has to fail with at least one of the two multi-player detection modes.
- If the mod is open-source or source available, **one** of the following two criteria must be met.
  - Enough downloads on Modrinth (100+ for the past week).
  - Pass checks on VirusTotal.
  - Pass manual audits from at least one of the maintainers.
- If the mod has zero source code available, **all** of the following criteria must be met.
  - Enough downloads on Modrinth (1000+ for the past week).
  - Pass checks on VirusTotal.
  - Zero obfuscation.
