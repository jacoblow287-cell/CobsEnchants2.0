# CobsEnchants

A custom enchant plugin for Paper 1.21.11. Run `/cobsenchants` (aliases `/ce`, `/customenchants`)
to open a GUI, click an enchant to receive an enchanted book, then combine that book with **any
item in the game** in an anvil to apply it.

## How it works (important to understand before you deploy this)

Real vanilla enchantments are registered in a data-driven registry and are quite restrictive
about what items they can go on. To let every one of these enchants go on *any* item, this
plugin does **not** register real vanilla Enchantment objects. Instead:

- Each custom enchant is tracked as a hidden tag (`PersistentDataContainer`) directly on the
  item's NBT data — one tag per enchant, storing its level.
- Lore lines listing the enchants (with roman numeral levels) and the enchant "glow" (glint)
  are generated from those tags automatically.
- An `PrepareAnvilEvent` listener reads those tags off the book/item you put in the second anvil
  slot and merges them into the result, following the same "same level = +1, capped at max
  level" rule vanilla enchants use, and blocking conflicting pairs.
- All the actual gameplay effects (evasion, true damage, potion effects, immunities, etc.) are
  applied by listeners that check an entity's equipped items for these tags — not by anything
  vanilla's enchantment system does automatically.

This is the standard, reliable approach plugins use for "custom enchants," and it's what makes
"apply to literally anything" and "no vanilla category restrictions" possible.

## What's included

**Special enchants**
- **Damage Evasion** (I–III): 25% chance per level (capped 75%) to fully cancel incoming damage.
- **Double Jump** (I): jump again mid-air to launch yourself in your look direction. Implemented
  with the classic "allow-flight + cancel PlayerToggleFlightEvent" trick, since Bukkit has no
  native double-jump event.
- **True Damage** (I–V): hitting a player deals 1 extra heart of true damage per level, bypassing
  armor/protection.
- **Lifesteal Attack** (I): steals 2 hearts from your target and heals you 2 hearts per hit.
- **Lightning Attack** (I): in a weapon, strikes lightning on whoever you hit; in armor/offhand,
  strikes lightning on whoever hits you.

**Utility enchants**
- **Invis Armour** (I): the armor piece keeps protecting you, but is hidden from other players'
  view. Implemented with Paper's fake-equipment packet API (`Player#sendEquipmentChange`), which
  is repeatedly re-sent every second so it survives normal equipment sync packets.
- **Blacksmith Enchant** (I): while sneaking, right-click cycles the trim material and left-click
  cycles the trim pattern on any equipped piece carrying this enchant. The current position in
  each cycle is stored on the item so it always continues from where you left off.

**Dynamic categories** (generated automatically from every `PotionEffectType` / `DamageCause`
the server knows about, so you get full coverage without hand-listing each one):
- **`<Effect> Effect`** (I–X): grants that potion effect continuously while worn in any
  armor/offhand/mainhand slot. Level = amplifier.
- **`<Effect> Attack`** (I): in a mainhand weapon, applies that effect to whoever you hit; in
  armor or offhand, applies that effect to whoever hits you (retaliation).
- **`<Effect> Immunity`** (I): while worn/held anywhere, you can't have that potion effect
  applied to you.
- **`<Damage Cause> Immunity`** (I): while worn/held anywhere, you take zero damage from that
  specific cause (fall, fire, void, cactus, lava, drowning, lightning, etc. — every
  `EntityDamageEvent.DamageCause` value on the server).

**Conflicts**: a self-`Effect` enchant and the matching `Immunity` enchant for the same potion
effect are mutually exclusive on the same item (mirrors how vanilla Protection/Fire Protection
can't combine) — everything else stacks freely.

**Dragon Breath Immunity fix**: the lingering dragon-breath cloud's damage doesn't always report
`DamageCause.DRAGON_BREATH` on every version (it can come through as `MAGIC`/`POTION_EFFECT`
depending on how the server applies it). `ProtectionListener` now also detects the cloud directly
by checking if the damager is an `AreaEffectCloud` with a dragon-breath particle, so the immunity
enchant catches it either way.

**Cactus Immunity**: vanilla doesn't have a dedicated "cactus" damage cause — cactus and sweet
berry bush damage both share `DamageCause.CONTACT`. The generated enchant is relabeled "Cactus &
Berry Bush Immunity" so it's clear what it covers, but note it will also block berry bush damage
(there's no way to separate the two at the event level).

## Building

You'll need JDK 21 and Maven. This project was written entirely as source only — I did not have
network access in this environment to download the Paper API and test-compile it, so please
build and check the console output the first time you run it.

```bash
mvn clean package
```

The finished jar will be at `target/CobsEnchants.jar`. Drop it into your Paper 1.21.11 server's
`plugins/` folder and restart.

## Known limitations / things to double check

- **Renaming during a custom-enchant anvil combine**: if vanilla doesn't recognize any change
  (because our enchants aren't "real" enchantments), the plugin builds the result off your base
  item directly. If you also try to rename the item in the same anvil action as adding a custom
  enchant, the rename may not carry over — apply the rename in a separate anvil step if that
  happens.
- **Anvil cost** is capped at 39 XP levels so items never lock as "too expensive."
- **Effect enchants** refresh every 2 seconds with a 4-second potion duration, so there's no
  visible flicker, but the effect will drop about 2–4 seconds after you unequip the item.
- **Double Jump** repurposes `setAllowFlight`. If you use another plugin that also manages
  flight (fly commands, etc.) for the same players, test for conflicts.
- **Blacksmith Enchant** cancels the interaction event when it triggers, to stop the shift-click
  from also placing/breaking a block, but a very fast repeated left-click could still be picked
  up as a regular attack elsewhere — there's a 250ms cooldown per player to keep scrolling smooth
  without spamming.
- **Invis Armour** re-broadcasts fake equipment every second for every online player pair, which
  is fine for normal server sizes but is O(players²) — if you run a very high player-count server
  and notice load from this, the interval in `InvisArmorTask` can be increased.
- **Lore is fully regenerated** from the enchant tags whenever an item changes. If you want to
  add your own custom lore text to items independent of enchants, you'll need to extend
  `LoreBuilder` to preserve it (currently it only shows the enchant list).
- Effect names/immunity names in the GUI are auto-generated from Minecraft's internal registry
  keys (e.g. `night_vision` → "Night Vision"), so the exact list of effects/damage causes you see
  depends on your server version's registries.

## Project layout

```
src/main/java/com/cobsenchants/
  CobsEnchantsPlugin.java        main plugin class
  enchants/                      enchant definitions + registry
  util/                          PDC storage, lore building, book creation, roman numerals
  gui/                           main menu / category list / level picker inventories
  listeners/                     GUI clicks, anvil merging, combat, immunities, double jump
  tasks/                         repeating task that keeps "Effect" enchants active
  commands/                      /cobsenchants command
```
