# MeatShield's DRG DPS Calculator
A passion project to understand the weapons of Deep Rock Galactic

I love playing DRG, but it's not always easy to tell which weapon Mod or Overclock will be more effective. On top of that, the information displayed in-game is often incomplete, obfuscated, or occasionally incorrect. My primary goals for this program are:
1. Provide a tool that the DRG community can use to objectively and constructively discuss, compare, and contrast different builds for each weapon 
2. Provide accurate and precise information about every aspect and mechanic of the weapons

This project is something to keep my coding skills sharp, find all the numbers and hidden mechanics, and do the math automatically to see which build for a Weapon performs the best. I hope that everyone in the DRG community (including the Developers) will find some benefit from the research and modeling done for this project.

Additionally, [Karl.gg](https://karl.gg/) uses the Mod and Overclock tooltips from this program, and the metrics calculated for each build get used in the Advanced Statistics View. All the work done for this project benefits everyone who uses that website, too!

If you want to contribute to this project, feel free to make a Pull Request and I'll take a look at your suggested changes. I'm also open to adding people as Collaborators to this project.

___
### How to Install
This program is a freeware, open-source fan project, so it's completely free to download and use. Start by going to the latest [release](https://github.com/drg-tools/drg-weapons-calculator/releases). If you use Windows for your OS, you can download and run the installer.exe attached to the release which will use an Installation Wizard to install and configure the program automatically. If you use either Mac or Linux, you'll need to install Java 8 or higher on your machine and then download the runnable JAR file from the release.

___
### What metrics does it calculate?
- Burst DPS vs a primary target
  - Enable the Weakpoint DPS flag to model it as if Weakpoint Accuracy percent of the bullets hit a Weakpoint
  - Enable the Accuracy DPS flag to model it as if only General Accuracy percent of the bullets hit the target
  - Enable the Armor DPS flag to model it as if a percentage of the Direct Damage gets reduced by enemies' Armor
- Sustained DPS vs a primary target (also affected by the three DPS flags)
- Sustained DPS vs a secondary target (ignores Status Effects and the three primary-target DPS flags)
- Theoretical maximum number of enemies hit per ammo consumed
- Theoretical maximum amount of damage dealt (assumes hitting the max number of enemies with every ammo consumed, without resupplying)
- Ammo Efficiency (how much "bang for your buck" you get for every shot)
- Average percentage of Direct Damage wasted by Armor on enemies
- Estimated General Accuracy (probability to hit an enemy anywhere)
- Estimated Weakpoint Accuracy (probability to hit an enemy's Weakpoint)
- How long it takes to expend all ammo without resupplying
- Average Time-to-Kill
- Average percentage of damage that is lost to "Overkill"
- A full list of 17 enemies' Breakpoints (how many shots it takes to kill), which accounts for hitting Weakpoints and breaking through Light Armor
- Utility, which covers most of the non-combat things:
  - Enhancing player Mobility
  - Giving the player Damage Resistance
  - Average probability to break a Light Armor plate
  - Slowing enemies
  - Inflicing Fear on enemies
  - Stunning enemies
  - Freezing enemies
- Average time required to Ignite or Freeze an enemy (if the Weapon is able to do either Heat or Cold with its attacks)

___
### What can it do?
After you finish installing it on your local computer, there are a wide variety of features available:
- There's an interactive Graphical User Interface ("GUI") that imitates DRG's Equipment Terminal, which gives it a very intuitive layout for anyone who's played the game
- Every Weapon in the game has its own tab in the GUI (grouped by Class), and every Mod and Overclock can be selected to customize your build. As the build fills out, the metrics in the bottom part of the GUI will automatically update with the new values.
  - Driller's EPC gets two models (Regular Shots and Charged Shots)
  - Engineer's Breach Cutter gets an additional model for its projectile
  - Scout's M1000 gets two models (Hipfired Shots and Focused Shots)
- Hovering your mouse over a Mod, Overclock, or Status Effect button for 0.25 seconds will bring up a Tooltip for 10 seconds that provides a textual description of what it does in-game.
- Want to know which build does the most DPS or total damage? Go to "Best Combinations (All)", select a metric, and let it run through every combination for you!
  - You can choose to enable any or all of the three DPS flags
  - If you want, you can have the program find the best combination for every weapon simultaneously (takes about 20-30 seconds)
- Want to find the best build for a certain metric that uses your favorite Mod or Overclock? "Best Combinations (Subset)" has your answers.
  - Any Mod or Overclock that you already have selected will be guaranteed to be in the final build
  - Right-click on a Mod or Overclock to exclude it from being part of the calculations
- By default, it starts out by modeling its metrics based on (Hazard 4/4 Player) Difficulty Scaling, but you can customize it to any Hazard level and any number of Players (excluding Deep Dive and Elite Deep Dive scaling)
- Export the data for every comination of Mods and Overclocks for every weapon! 
  - Export a CSV file for only the selected model
  - Export the CSV files for all models simultaneously
  - Export every combination of every model in one giant MySQL table
- Load up to four builds simultaneously for a single model and compare their outputs! 
  - You can compare them metric-to-metric, and then compare their Stat Panels and Breakpoints granularly. 
  - Alternatively, you can see graphs of hitscan weapons' Accuracy and related DPS metrics as the distance ranges between 1m and 19m. You're also able to save screenshots of the graphs generated!
- Want to save a weapon build for quick reference or to share with your friends? This program can take screenshots of itself! Go to "Misc. Actions -> Save screenshot of current build", select where you want to save the picture, and you're good to go!
- Certain semi-automatic weapons allow you to set a custom Rate of Fire. Currently, Driller's Subata and EPC (Regular Shots), Engineer's Shotgun, and Gunner's Revolver are the four models that have this feature enabled.
- You can toggle certain Status Effects on and off to see how they interact with the Mods and Overclocks. In particular, it's able to model:
  - Burning
  - Frozen
  - Electrocuted
  - Scout's IFG
- For the hitscan weapons in DRG, this program is able to accurately predict how the crosshairs will expand and move on the screen and then calculate the probability that each shot will hit an enemy (General Accuracy) or an enemy's weakpoint (Weakpoint Accuracy). Beyond that, it's also able to provide an animated visualization of what it looks like, and you can customize several settings about how the program estimates Accuracy:
  - Whether the dwarf is moving
  - Enable/disable Recoil
  - Change the red target circle in the visualizations from a General size to the Weakpoint size
  - Customize the distance from which the Accuracy gets calculated, ranging between 1m and 19m
- For the Damage Lost to Armor, Average Overkill, Breakpoints, and Utility metrics you can click on the numbers to bring up a granular list of the individual values that get used to calculate the number. Utility just adds them all up, but the other three use a weighted average vector based on enemies' spawn probabilities.
- Have questions about what something means? Take a look in the "Information" tab to the right of Scout! There's a more in-depth explanation of what the metrics are, an FAQ section, and a Glossary of terms used by the program.
- If you find a bug, think something is modeled incorrectly, or want to suggest a feature, go to "Misc. Actions -> Suggest a change for this program". It will automatically open up the GitHub Issue creator for this repository in your web browser.

___
### Technical Specifications
* Compiled on Java JRE 12.0.2, Class v56
* Bundled for Windows using Launch4j 3.12 and Inno Setup 6.0.4

___
According to CLOC, 1.1.0's metrics:
* 96 .java files
* 3804 blank lines
* 2572 comment lines
* 18648 code lines