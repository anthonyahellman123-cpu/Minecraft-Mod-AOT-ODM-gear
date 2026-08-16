# ODM Gear

A Forge 1.20.1 Minecraft mod focused on polished, momentum-driven ODM movement and high-speed sword combat.

## Design pillars

- Cables redirect momentum instead of instantly pulling the player to an anchor.
- Gas supplies acceleration and combat assistance rather than creative-style flight.
- Releasing a cable preserves velocity.
- Fast ground contact can become a slide instead of an immediate stop.
- Movement should be fun even when there is nothing to fight.

## Current status

The Forge project skeleton is established. Movement, gas, grappling, lock-on, and weapons are not implemented yet.

## Development

- Minecraft: 1.20.1
- Forge: 47.4.10
- Java: 17

Build the mod with:

```bash
./gradlew build
```

The built mod JAR will be written to `build/libs/`.
