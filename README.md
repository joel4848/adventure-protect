
# Adventure Protect

Prevents players in adventure mode from interacting with certain blocks/entities. Protection can be enabled/disabled for each type of block/entity in the config, or for specific blocks/entities in-game.

The mod can be used 100% server-side only, but anyone who wants to use the exception tool (see 'Configurability' below) will need it on the client too.

_I made this mod for my own needs on my own server, and I'm only making it public to make it easier for my players to get. It's tested only on Fabric 1.21.1._
## Features

**Protection**

Prevents players in Adventure Mode from:

- Opening chests, barrels or shulker boxes
- Stealing flowers from plant pots, or putting new ones in
- Opening/closing trapdoors
- Breaking item frames/glow item frames
- Rotating items in item frames
- Interacting with note blocks or jukeboxes
- Putting items into decorated pots
- Breaking minecraft paintings
- Interacting with brewing stands
- Placing, replacing or removing items on/from armour stands
- Breaking easels from [Joy of Painting (XercaPaint)](https://modrinth.com/mod/joy-of-painting)
- Breaking/rotating paintings from JoP that have been placed in the world
- Breaking/rotating/resizing placed photographs from [Camerapture](https://modrinth.com/mod/camerapture)
- Interacting with the music box and metronome from [Music Maker Mod (XercaMusic)](https://modrinth.com/mod/music-maker-mod), as well as pianos and drum kits placed as blocks

**Configurability**

All of the different protection categories above can be enabled/disabled using the command `/adventureprotect protect <block/entity category> <true|false>` as well as in the config file.

The mod also adds *'The Exceptionator'* (run `/adventureprotect exceptionator`) - shift right-click on *most* of the things above to add protection exceptions to specific blocks/entities. Want your players to be able to open specific chests/barrels/shulker boxes? No problem, just Exceptionate it! Need them to be able to rotate an item in an item frame to open a secret door? No worries, just Exceptionate it!


## License

[CC BY-NC-SA 4.0](https://creativecommons.org/licenses/by-nc-sa/4.0/)
