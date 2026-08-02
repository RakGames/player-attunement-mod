# Player Attunement Mod (PAM)

**MMO-style attunement gating for Minecraft.** Lock dimensions, gear, and mechanics behind progression milestones — the same way raid/dungeon attunements work in MMOs — with three ready-to-go difficulty presets and a fully datapack-driven rule engine underneath.

- **Mod ID:** `pam`
- **Author:** lindvall.io
- **Target:** Minecraft 26.2, NeoForge
- **License:** All Rights Reserved *(see note below)*

---

## What it does

PAM stops players from waltzing into the Nether in diamond armor five minutes into a fresh world — or lets you build exactly that kind of gate yourself. Access to dimensions, armor/tools, right-click items, totems, beacons, ender chests, and shulker boxes can all be locked behind **attunements**: flags a player earns by hitting real milestones (advancements, crafting specific gear, killing bosses, reaching an XP level). Nothing is hardcoded — every rule lives in JSON and can be redefined, extended, or replaced by a datapack.

On first joining a new world, singleplayer players get a **three-box splash-art picker** to choose their pace. Dedicated servers skip the UI entirely and read `config/pam-server.toml` instead.

## Progression presets

| Preset | Vibe | What's gated |
|---|---|---|
| **Sandbox** (`none`) | Vanilla, PAM stays out of the way | Nothing — every lock is a no-op |
| **Vanilla+** (`vanilla_plus`) | Light-touch pacing | Nether/End entry, Elytra flight — each gated behind its natural vanilla milestone (full diamond armor, a blaze rod + Netherite gear, slaying the Ender Dragon) |
| **RPG Progression** (`attunement_progression`) | Full MMO-style gating | Everything in Vanilla+, *plus* diamond gear (Suit Up + level 20), Netherite gear (Fortress + Ancient Debris), shulker boxes & ender chests (levitation), Totems of Undying (Hero of the Village), and beacons |

The choice is **sticky per world** — once resolved, it doesn't change unless an operator runs `/pam preset set`.

## Admin commands

All gated behind operator permission (`Commands.LEVEL_GAMEMASTERS`, the old op-level-2 equivalent):

```
/pam grant <player> <attunement_id>       Grant a specific attunement
/pam revoke <player> <attunement_id>      Revoke a specific attunement
/pam list [player]                        List a player's unlocked attunements
/pam clear <player>                       Wipe all of a player's attunements
/pam preset get                           Show the world's active preset
/pam preset set <none|vanilla_plus|attunement_progression>   Change it
```

## For modpack authors: it's all datapack-driven

Presets live in `data/<namespace>/presets/*.json` as a name plus a list of attunement definitions:

```json
{
  "id": "pam:my_preset",
  "name": "My Custom Pacing",
  "attunements": [
    {
      "id": "pam:nether_entry",
      "requirements": {
        "advancements": ["minecraft:story/shiny_gear"],
        "min_player_level": 10
      },
      "locks": { "dimensions": ["minecraft:the_nether"] },
      "denial_message": "pam.denied.nether_entry"
    }
  ]
}
```

Requirement categories combine with **AND** across categories, but **ANY ONE** entry satisfies `items_crafted` / `entities_killed` (listed **advancements** must all be earned). Lock categories: `dimensions`, `items` (armor/tools/right-click/totems), `blocks` (beacons, chests, shulkers), `entities` (exposed for API consumers).

Add or override presets from any datapack — no code required. Ship a resource pack namespace conflict? Higher-priority packs win, same as any other vanilla data.

## For mod/script developers

Public API lives in `com.zenil.pam.api`:

- `AttunementApi` — query, grant, revoke, and evaluate attunements programmatically
- `AttunementCheckEvent` — force-allow or force-deny any lock check from another mod, KubeJS, or CraftTweaker
- `AttunementUnlockEvent` — react when a player earns a new attunement

## Server configuration

`config/pam-server.toml`, auto-generated on first startup:

```toml
[general]
    #The progression preset applied to any world on this server that has not chosen one yet.
    #One of: none, vanilla_plus, attunement_progression
    default_preset = "vanilla_plus"
```

## Requirements

- Minecraft **26.2**
- **NeoForge** `26.2.0.41-beta` or later
- Works in **singleplayer and dedicated servers**. Both client and server need the mod installed — PAM syncs attunement state and denial notices over the network and isn't purely a server-side or client-side mod.

## Known limitations

- No full HUD progression tracker yet — denial notices sync over the network, but a richer in-game progress UI is a future-release idea.
- Melee attacking with a locked weapon isn't intercepted (equip, right-click use, and block-breaking are).
- `/pam preset set` doesn't retroactively re-check online players against newly-active locks — they pick up new attunements on their next relevant trigger (advancement, craft, kill, level-up).
- Entity-interaction locks are exposed in the schema/API but have no built-in handler; wire your own via `AttunementApi.evaluate(..., LockType.ENTITY, ...)`.

See [`release-notes/1.0.0.md`](release-notes/1.0.0.md) for the full changelog.

## Installation

Drop `pam-mc26.2_1.0.0.jar` into your `mods` folder alongside NeoForge. No other dependencies.

---

### A note on the license

This is currently listed as **All Rights Reserved** — a placeholder set during development, not a considered choice for public release. That means, as written, nobody (including modpack curators) can legally bundle or redistribute PAM without asking first. Worth deciding before this goes up: something like `LGPL-3.0`, `MIT`, or an "All Rights Reserved, modpacks OK" carve-out are all common choices for NeoForge mods depending on how much reuse you want to allow.
