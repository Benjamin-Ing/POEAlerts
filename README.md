# POEAlerts

After losing a character in Hardcore due to not reading reflect on a map mod, I decided to make POEAlerts.

POEAlerts will rigger an alert sound whenever you click on an item that contains text found in the accompanying alerts.txt file.
The file may look something like:

  % of elemental damage
  % of physical damage
  monster critical strike multiplier
  cannot regenerate
  chaos resistance, maximum life

This means an alert is triggered whenever you click on an item that has "cannot regenerate".
This can help to avoid dangerous map mods.

As well, the line "chaos resistance, maximum life" can alert you when you're vendoring items.
Perhaps you would rather keep that armor piece with Chaos Res and Life.
Note that comma separated text requires each one to be found on the item.
Whereas line separated text are searched separately.
