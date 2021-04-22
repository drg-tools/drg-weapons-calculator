package guiPieces;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Insets;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import modelPieces.DoTInformation;
import modelPieces.EnemyInformation;
import modelPieces.UtilityInformation;
import utilities.MathUtils;

public class InformationTabsText {

	private static JPanel createScrollableTextPanel(String title, JPanel scrollableText) {
		 JPanel toReturn = new JPanel();
		 toReturn.setLayout(new BorderLayout());
		 toReturn.setBackground(GuiConstants.drgBackgroundBrown);
		 
		 JLabel header = new JLabel(title);
		 header.setForeground(GuiConstants.drgRegularOrange);
		 header.setFont(GuiConstants.customFontHeader);
		 JPanel centerAlignHeader = new JPanel();
		 centerAlignHeader.setLayout(new FlowLayout(FlowLayout.CENTER));
		 centerAlignHeader.setBackground(GuiConstants.drgBackgroundBrown);
		 centerAlignHeader.add(header);
		 toReturn.add(centerAlignHeader, BorderLayout.NORTH);
		 
		 JScrollPane scrollable = new JScrollPane(scrollableText);
		 scrollable.getVerticalScrollBar().setUnitIncrement(12);
		 toReturn.add(scrollable, BorderLayout.CENTER);
		 
		 return toReturn;
	}
	
	// This method can pull double-duty as both a Q&A box as well as a Term/Definition box
	private static JPanel createQandAPanel(String question, String answer) {
		JPanel toReturn = new JPanel();
		toReturn.setLayout(new BoxLayout(toReturn, BoxLayout.PAGE_AXIS));
		
		JLabel questionOrTerm = new JLabel(question);
		questionOrTerm.setFont(GuiConstants.customFontBold);
		questionOrTerm.setForeground(GuiConstants.drgRegularOrange);
		// Set the Label to be almost flush with the left side
		JPanel leftAlignLabel = new JPanel();
		leftAlignLabel.setLayout(new FlowLayout(FlowLayout.LEFT));
		leftAlignLabel.setBackground(GuiConstants.drgBackgroundBrown);
		leftAlignLabel.add(questionOrTerm);
		toReturn.add(leftAlignLabel);
		
		JTextArea answerOrDefinition = new JTextArea(answer);
		answerOrDefinition.setFont(GuiConstants.customFont);
		answerOrDefinition.setBackground(GuiConstants.drgBackgroundBrown);
		answerOrDefinition.setForeground(GuiConstants.drgHighlightedYellow);
		// Left-pad the answer a bit for visual clarity
		answerOrDefinition.setMargin(new Insets(0, 30, 8, 22));
		answerOrDefinition.setWrapStyleWord(true);
		answerOrDefinition.setLineWrap(true);
		toReturn.add(answerOrDefinition);
		
		return toReturn;
	}
	
	public static JPanel getMetricsExplanation() {
		String[][] metricsExplanationtext = {
			{"Burst DPS", "This metric is the maximum Damage Per Second that any weapon can do before having to either reload or cool down. For weapons with a magazine size larger than 1, this "
					+ "metric represents what the DPS would be while emptying an entire magazine at max rate of fire into an enemy. DoTs have their DPS added to this metric multiplied by the "
					+ "coefficient of how long the DoT would damage the enemy during the magazine firing divided by how long it takes to empty the magazine. If the weapon only fires 1 shot "
					+ "before reloading, then this is the damage of that single shot divided by reload time, and DoTs are multiplied by the estimated percentage of enemies that the single shot "
					+ "would ignite. There are three buttons that can enable/disable whether or not Weakpoint damage is modeled, whether or not projectiles miss their target due to General "
					+ "Accuracy, and whether or not Direct Damage gets reduced by the Armor Wasting metric."},
			{"Sustained DPS", "Very similar to Burst DPS, this metric models what the average DPS would be if you were to fire every magazine at max RoF, reload, and continue firing again until the "
					+ "weapon ran out of ammo. Because this metric models the average DPS over a long period of time, DoTs have their full DPS added to this value. The Weakpoint, Accuracy, and "
					+ "Armor Wasting toggles all affect this metric just like Burst DPS."},
			{"Additional Target DPS", "If the currently selected weapon can hit more than one enemy per projectile, then this metric will represent what the average Sustained DPS dealt to "
					+ "non-primary targets would be. This metric is modeled without respect to Weakpoints, Accuracy, or Armor Wasting."},
			{"Max Num Targets", "This metric represents the theoretical maximum number of Glyphid Grunts that take damage from a single projectile fired by the current weapon. For weapons "
					+ "that deal splash damage, like Engineer's Grenade Launcher or Gunner's Autocannon, you can click on this metric to see a visualization of how this program estimates "
					+ "enemies hit by a splash radius."},
			{"Max Multi-Target Damage", "As the name implies, this metric is used to show how much damage can be dealt by this weapon without having to resupply. This is modeled as if every "
					+ "single projectile hits a primary target and all possible secondary targets, and DoT damage dealt to individual enemies contributes to this value as well. As a result, "
					+ "getting a higher number of Max Num Targets will scale this number just as strongly as carrying more ammo."},
			{"Ammo Efficiency", "Ammo Efficiency is a bit more abstract of a metric, and technically doesn't have any units associated with it (unlike DPS, num targets, max damage, etc). "
					+ "The current formula used to calculate Ammo Efficiency is (Max Multi-Target Damage / Math.ceil(Number of bullets needed to kill one enemy, including Weakpoint Bonuses)). "
					+ "As a result of that formula, higher damage per bullet and higher Weakpoint bonus will yield a smaller denominator, while higher damage per bullet, more targets per shot, "
					+ "and more carried ammo will result in a higher numerator. Using a combination of those 4 upgrades will result in a very high AE score. Higher AE allows you to deal with "
					+ "more enemies without needing as many resupplies."},
			{"Average Damage Lost vs Armored Enemies", "Certain enemies in DRG have Light, Medium, and Heavy Armor on them which either reduces or negates incoming Direct Damage until the Armor "
					+ "plates are broken off. This metric is an estimate of how much total damage has to be dealt to those enemies to both break through their Armor plates and kill them, and how "
					+ "much damage is \"wasted\" by Armor as a result. Click on the button to see the list of Armored enemies modeled, along with the percentage wasted per creature."},
			{"Accuracy Visualizer", "For weapons that use elliptical crosshairs, there will be a button that brings up a detailed overview of the variables used to generate the Accuracy percentages seen, "
					+ "graphs of Spread, Recoil, and player-reduced Recoil, an animation of \"raw\" Accuracy, and an animation of player-affected Accuracy."},
			{"General Accuracy", "A pretty straight-forward metric to understand, General Accuracy is an estimate of what percentage of projectiles would hit a target from 4-10m away using sustained "
					+ "fire. Some weapons like both of Driller's primary weapons, Engineer's secondary weapons, or Scout's M1000 Classic (Focused Shots) can't have their accuracy modeled and will "
					+ "instead say \"Manually Aimed\". Clicking on General Accuracy will bring up a Settings panel that will let you change how the Accuracy gets modeled."},
			{"Weakpoint Accuracy", "Just like General Accuracy, this metric represents what percentage of projectiles would hit an enemy's Weakpoint from 4-10m away. For weapons that can't have their "
					+ "Accuracy modeled, it will instead say \"Manually Aimed\"."},
			{"Firing Duration", "This answers the question of how long it will take to fire every projectile from the weapon if you were to fire continuously, even through reloads or cooldowns. "
					+ "Slower rates of fire and large carried ammo capacities increase the duration, whereas faster rates of fire and faster reloads decrease duration."},
			{"Avg Time to Kill", "A very simple metric; all this does is divide the weighted average healthpool of all enemies by the current Sustained + Weakpoint DPS to get an estimate of "
					+ "how quickly the current weapon and build can kill an enemy."},
			{"Avg Overkill", "This is an estimate of how much damage gets \"wasted\" by bullets when enemies have lower remaining health than the damage per projectile. Because different creatures have "
					+ "different healthpools that scale with Hazard Level and Player Count, this uses a weighted average of all enemies' healthpools for its Overkill calculations."},
			{"Breakpoints", "Although the number displayed is pretty meaningless by itself, clicking on this metric will have a small window pop up that shows you the fewest number of projectiles "
					+ "needed to kill various enemies under different conditions. Weapons that shoot multiple pellets per shot (Engineer's Shotgun and Scout's Boomstick) have the damage per shot multiplied by "
					+ "General Accuracy to be slightly more realistic."},
			{"Utility", "Another abstract metric, this tries to numerically represent the value of certain mods that don't affect DPS or total damage, but do things like slow or stun enemies. "
					+ "Additionally, if the weapon can break Light Armor Plates, then the average probability that each shot can break a Light Armor plate will be listed."},
			{"Average Time to Ignite or Freeze", "As the name implies, if a weapon can deal either Heat Damage or Cold Damage, then this number will tell you the average time it would take to ignite or freeze "
					+ "enemies. If this value is zero, that means that so much Heat or Cold Damage is being done in a single burst that it instantly ignites or freezes all enemies."},
			// {"Haz5 Viable", "TODO"},
		};
		
		JPanel panelContainedWithinScrollPane = new JPanel();
		panelContainedWithinScrollPane.setBackground(GuiConstants.drgBackgroundBrown);
		panelContainedWithinScrollPane.setLayout(new BoxLayout(panelContainedWithinScrollPane, BoxLayout.PAGE_AXIS));
		
		for (int i = 0; i < metricsExplanationtext.length; i++) {
			panelContainedWithinScrollPane.add(createQandAPanel(metricsExplanationtext[i][0], metricsExplanationtext[i][1]));
		}
		
		return createScrollableTextPanel("What do each of the calculated metrics mean?", panelContainedWithinScrollPane);
	}
	
	public static JPanel getFAQText() {
		String[][] FAQtext = {
			{"Why do some Mods and Overclocks have a Red outline?", "Mods or Overclocks with a Red outline can't be represented by the Weapon's stats because how they work in-game doesn't affect how I've chosen to model the Weapon."},
			{"What's the point of this program?", "To help the DRG community compare and contrast their preferred builds for each weapon, and to provide more detail about how the weapons work than described either in-game or on the wiki."},
			{"How long should I wait for the program to calculate the best build?", "This should run pretty fast. I would expect it to be done in a second or two, ten at most."},
			{"I think something is wrong/missing, how do I communicate that to you?", "In the 'Misc. Actions' Menu, there's an option to suggest changes. That should automatically open up this project's GitHub issue creation page for you."},
			{"Can I help improve to this project?", "Yes! This is an open-source, freeware fan project. Although it's started out as just one developer, I would love to have help."},
			{"How frequently will this be updated?", "This is pretty much the final product in terms of functionality, but I'm planning to release an update with every weapon balance patch for DRG."},
			{"Will this be made available as a live website?", "Probably not. Thousands of lines of Java code do not port well into HTML/CSS/Javascript. The data generated by this program is currently available on Karl.gg in the "
					+ "Advanced Statistics View, and they're working on adding it to the Build page too."},
			{"How did you model [insert mechanic here]?", "This is an open-source project. Feel free to look around the source code and see how it was done. In general though: I chose to model everything like a continuous function instead of "
					+ "discrete. Slight loss of precision, but significantly easier."},
			{"How are Status Effect Utility scores calculated?", "The formula I chose to use is (% Chance to Proc) * (Number of Targets) * (Effect Duration) * (Utility Factor), where 'Utility Factor' is some scalar value assigned to each effect."},
			// I'm intentionally adding blank lines below here so that the content gets pushed to the top of the page
			{"", ""},
			{"", ""},
			{"", ""},
			{"", ""},
			{"", ""},
			{"", ""},
			{"", ""},
			{"", ""},
			{"", ""},
			{"", ""},
			{"", ""},
			// {"", ""},
		};
		
		JPanel panelContainedWithinScrollPane = new JPanel();
		panelContainedWithinScrollPane.setBackground(GuiConstants.drgBackgroundBrown);
		panelContainedWithinScrollPane.setLayout(new BoxLayout(panelContainedWithinScrollPane, BoxLayout.PAGE_AXIS));
		
		for (int i = 0; i < FAQtext.length; i++) {
			panelContainedWithinScrollPane.add(createQandAPanel(FAQtext[i][0], FAQtext[i][1]));
		}
		
		return createScrollableTextPanel("Frequently Asked Questions", panelContainedWithinScrollPane);
	}

	public static JPanel getGlossaryText() {
		// Perhaps these should be sorted alphabetically?
		String[][] glossaryText = {
			{"Armor", "Some of the enemies on Hoxxes IV have exterior armor plates to protect them. Grunts have Light Armor that reduces damage by 20%, but has a chance to break every time it gets damaged. Praetorians and "
					+ "Shellbacks have Heavy Armor plates that negate all incoming Direct Damage, but break after absorbing a set amount of damage. The third type of armor is found on Oppressors and Dreadnoughts: it makes them "
					+ "immune to all Direct Damage from the front and can't be broken, so shoot their abdomen."},
			{"Armor Breaking", "Increasing this stat above 100% means that it takes fewer shots to break Grunt and Praetorian armor plates, so you lose less damage to Armor. Doesn't affect the third type of Armor, though. "
					+ "Likewise, if this is less than 100%, it means that damage is less effective vs Armor."},
			{"Weakpoint", "Most enemies have certain spots on their body that will take extra Direct Damage. Those spots are referred to as Weakpoints. Common areas are the mouths of medium-sized Glyphids, the abdomens of "
					+ "Macteras, Praetorians, and Dreadnoughts, and glowing bulbs on the sides of larger enemies like Bulk Detonators, Wardens, and Menaces. With the exception of mouths, Weakpoints are usually brightly colored and will light up when damaged."},
			{"Weakpoint Bonus", "Some weapons that deal Direct Damage have Mods or Overclocks that affect how that damage gets multiplied when impacting a Weakpoint. Most of the time, it's a multiplicative bonus that gets applied to the projectile before the Weakpoint's multiplier gets used."},
			{"Direct Damage", "One of the three main Types of damage dealt by weapons, Direct Damage is the only one of the three that is affected by Armor, Weakpoints, and the Frozen Status Effect. Direct Damage gets reduced when passing through Light Armor, negated entirely by Heavy Armor "
					+ "and Unbreakable Armor, and gets multiplied when impacting a creature's Weakpoint or a Frozen enemy. Direct Damage can be any of these Elements: Kinetic, Disintegrate, Explosive, Fire, Frost, or Electric."},
			{"Area Damage", "The second of the three main Types of damage, Area Damage ignores Armor, Weakpoints, and the Frozen Status Effect. Any Area Damage inflicted to a creature simply reduces their healthbar. Not all Area Damage is dealt in an Area-of-Effect, like Embedded Detonators "
					+ "or Explosive Reload. Area Damage can be any of these Elements: Disintegrate, Internal, Explosive, Fire, Frost, or Electric"},
			{"Temperature Damage", "The third Type of damage dealt by weapons, all forms of Temperature Damage only affect a creature's Temperature meter and do not directly affect the enemies' healthbars. Heat increases the Temperature Meter, and Cold decreases it."},
			{"Elemental Damage Types", "All Direct Damage, Area Damage, and Damage Over Time is comprised of one or more of the following types: Kinetic, Disintegrate, Internal, Explosive, Fire, Frost, Electric, Poison, or Radiation. Depending on the creature being shot and the biome you're in, each of these "
					+ "elements can either be more effective against or resisted by the creature."},
			{"Heat (Temperature Damage)", "Heat Damage doesn't actually affect an enemy's healthbar; rather it affects their Temperature. Once enough Heat has been accumulated, the enemy receives a Burn DoT which continues until the enemy "
					+ "sheds enough Heat to be doused, or the enemy dies. Applying more Heat to an already ignited enemy prolongs the Burn duration. Heat counteracts Cold, and can cause Temperature Shock if applied to a Frozen enemy."},
			{"Cold (Temperature Damage)", "As Frost element Damage is to Fire, so Cold is to Heat. Applying Cold to an enemy decreases their Temperature until eventually they become Frozen. Enemies remain Frozen until they gain enough Heat to thaw, "
					+ "at which point they can start accumulating Cold again. Applying more Cold to Frozen enemies does NOT increase the Freeze Duration. Cold counteracts Heat, and can cause Temperature Shock if applied to a Burning enemy."},
			{"DoT", "An acronym that stands for \"Damage Over Time\". This term is used to refer to damage which doesn't get applied per projectile, but rather gets applied over a period of several seconds."},
			{"Status Effect", "A conditional effect that can be applied to enemies. Sometimes it's a DoT, other times it's a crowd control effect."},
			{"Burn (DoT)", "When an enemy has its Temperature meter increased to maximum by taking sustained Heat, it ignites and gains a Burn DoT. While Burning, enemies take an average of " + MathUtils.round(DoTInformation.Burn_DPS, GuiConstants.numDecimalPlaces) 
					+  " Fire Damage per second. If no more Heat is applied, then their Temperature will steadily decrease until they are doused and the Burn DoT will end. On the other hand, sustaining even more Heat will prolong the Burn duration. Applying "
					+ "Cold will significantly shorten the duration of the Burn, but also inflict Temperature Shock for 200 Disintegrate element Damage."},
			{"Frozen (Status Effect)", "Thematically the opposite of the Burn DoT, enemies become Frozen when their Temperature is lowered enough by sustained Cold. Once Frozen, they receive x" + UtilityInformation.Frozen_Damage_Multiplier + " Direct Damage. Frozen enemies "
					+ "cannot have the freeze duration increased; instead they will thaw over time. Once they have thawed, more Cold can be applied to freeze them again. Applying Heat will significantly shorten the duration of the Freeze, but also inflict Temperature Shock for 200 Disintegrate element Damage."},
			{"Electrocute (DoT, Status Effect)", "Some of the Weapons and Overclocks have a chance to apply the Electrocute Status Effect. Once applied, enemies take an average of " + MathUtils.round(DoTInformation.Electro_DPS, GuiConstants.numDecimalPlaces) + " Electric Damage per second for " 
					+ DoTInformation.Electro_SecsDuration + " seconds, while also being slowed by 80%. Enemies can only have one Electrocute applied to them at once; if another shot were to apply a second Electrocute, the first DoT has its duration refreshed instead."},
			{"Radiation (DoT)", "There are two types of Radiation: environmental hazards in the Radioactive Exclusion Zone which deal an average of " + MathUtils.round(DoTInformation.Rad_Env_DPS, GuiConstants.numDecimalPlaces) + " Radiation Damage per second to the player, and the Radiation field left behind by the "
					+ "Overclock 'Fat Boy', which does an average of " + MathUtils.round(DoTInformation.Rad_FB_DPS, GuiConstants.numDecimalPlaces) + " Radiation Damage per second to enemies."},
			{"Neurotoxin (DoT, Status Effect)", "Similar to Electrocute, a few weapons can have a chance to apply Neurotoxin. Enemies afflicted by Neurotoxin take an average of " + MathUtils.round(DoTInformation.Neuro_DPS, GuiConstants.numDecimalPlaces) + " Poison Damage per second for up to " 
					+ DoTInformation.Neuro_SecsDuration + " seconds, while also being slowed by 30%. Also like Electrocute, enemies can only have one Neurotoxin DoT applied to them at once; anything that would apply a second effect instead refreshes the duration."},
			{"Persistent Plasma (DoT)", "Similar to Radiation, this is an area-of-effect DoT that gets left behind by certain mods and overclocks. It deals an average of " + MathUtils.round(DoTInformation.Plasma_EPC_DPS, GuiConstants.numDecimalPlaces) + " Fire Damage per second."},
			{"Stun (Status Effect)", "Stunning an enemy stops them from moving or attacking for a set duration. That duration changes from weapon to weapon, but it's typically around 2 seconds. Enemies that channel their attacks (like Praetorians) can have those attacks interrupted by a Stun."},
			{"Fear (Status Effect)", "Inflicting Fear on an enemy causes them to stop what they're doing and run from the source of the Fear as fast as they can move for about " + MathUtils.round(EnemyInformation.averageFearDuration(), GuiConstants.numDecimalPlaces) + " seconds. After the Fear "
					+ "wears off, they return to normal behavior. Slowing, Stunning, or Freezing an enemy that is fleeing in Fear will effectively increase the Fear's duration."},
			{"Base Spread", "This stat affects how accurate the first shot will be. At 0%, that means the first shot is guaranteed to go exactly where your crosshair is pointing. As the percentage goes higher, the probability that the first shot will hit decreases."},
			{"Spread Per Shot", "After every shot gets fired, the maximum area of the crosshair increases by this amount. Thus, successive shots get increasingly less likely to hit your intended target until it reaches Max Spread."},
			{"Spread Recovery", "This stat is constantly reducing the current Bloom of the gun, trying to return to Base Spread. Because this is a constant rate, it's more effective the lower the Rate of Fire."},
			{"Max Bloom", "This value is added to Base Spread to determine the Max Spread. Spread per Shot can't add to the current Bloom value if it would go beyond Max Bloom."},
			{"Recoil Pitch", "Recoil Pitch determines how far the HUD gets moved vertically per shot."},
			{"Recoil Yaw", "Recoil Yaw determines how far the HUD gets moved horizontally per shot."},
			{"Mass (Recoil)", "Mass affects both how far the HUD moves in relation to Recoil Pitch and Yaw, as well as how long it takes for the weapon to recover from the Recoil per Shot."},
			{"Spring Stiffness (Recoil)", "Spring Stiffness determines how much this weapon resists moving around due to recoil, as well as helping it to return to center more quickly."},
			{"Time to Kill (TTK)", "As the name implies, this metric is used to estimate how quickly a weapon can kill an enemy. Because there are so many enemy types with different healthbars and spawn rates, this metric gets evaluated as the weighted average of all "
					+ "healthbars divided by the Sustained Weakpoint DPS of the weapon."},
			{"Overkill", "Because enemy healthbars don't normally come in multiples of the damage done by each Weapon, there's inevitably some damage going to be wasted. Overkill is an approximation of how much damage gets wasted by a Weapon as it kills the weighted average healthbar."},
			{"Mobility", "Some Mods or Overclocks can affect how efficiently players move around the environment. Often, they are conditional increases or decreases to movement speed, but sometimes they provide the ability to 'Blast Jump'."},
			{"Breakpoints", "In a general sense, Breakpoints are how few shots are necessary to kill certain creatures. As a result, higher damage per shot results in lower breakpoints. Glyphid Swarmers, Grunts, and Praetorians, "
					+ "as well as Mactera Spawns, are the most common breakpoints referenced."},
			{"Utility", "This is a generic term used as an umbrella for a variety of non-damage statistics, like buffs to the player or debuffs to enemies."},
			// {"", ""},
		};
		
		JPanel panelContainedWithinScrollPane = new JPanel();
		panelContainedWithinScrollPane.setBackground(GuiConstants.drgBackgroundBrown);
		panelContainedWithinScrollPane.setLayout(new BoxLayout(panelContainedWithinScrollPane, BoxLayout.PAGE_AXIS));
		
		for (int i = 0; i < glossaryText.length; i++) {
			panelContainedWithinScrollPane.add(createQandAPanel(glossaryText[i][0], glossaryText[i][1]));
		}
		
		return createScrollableTextPanel("Glossary of Terms", panelContainedWithinScrollPane);
	}
	
	public static JPanel getAcknowledgementsText() {
		String[][] acknowledgementsText = {
			{"Ghost Ship Games", "Thank you for making the game Deep Rock Galactic and letting me use some images and artwork from the game in this program."},
			{"Mike @ GSG / Dagadegatto", "Thank you for being willing to answer so many of my technical questions about DRG and helping to improve the quality of this program's models."},
			{"Elythnwaen", "Thank you for collecting data about elemental weaknesses, resistances, Burn/Freeze temperatures, and more! Also, thank you for finding a bunch of information for me."},
			{"GreyHound", "Thank you for looking through this source code and finding incorrect values, as well as finding some values in the game files."},
			{"Ian McDonagh", "Thank you for creating the open-source JAR 'image4j' that allows me to use .ico files natively."},
			{"Chris Kroells", "Thank you for creating the open-source JAR 'thumbnailator' that does a better job compressing image files than Java Graphics2D can do natively."},
			{"USteppin", "Thank you for collecting some data and testing weapon builds for me on Hazard 5. Twitch Channel: https://www.twitch.tv/usteppin"},
			{"Alpha and Beta testers", "Thank you Minomess, Royal, CynicalAtropos, ARobotWithCancer, and ArcticEcho for giving me feedback while this was still being developed and helping test out the builds."},
			// I'm intentionally adding blank lines below here so that the content gets pushed to the top of the page
			{"", ""},
			{"", ""},
			{"", ""},
			{"", ""},
			{"", ""},
			{"", ""},
			{"", ""},
			{"", ""},
			{"", ""},
			{"", ""},
			{"", ""},
			{"", ""},
			{"", ""},
			{"", ""},
			{"", ""},
			{"", ""},
			{"", ""},
			// {"", ""},
		};
		
		JPanel panelContainedWithinScrollPane = new JPanel();
		panelContainedWithinScrollPane.setBackground(GuiConstants.drgBackgroundBrown);
		panelContainedWithinScrollPane.setLayout(new BoxLayout(panelContainedWithinScrollPane, BoxLayout.PAGE_AXIS));
		
		for (int i = 0; i < acknowledgementsText.length; i++) {
			panelContainedWithinScrollPane.add(createQandAPanel(acknowledgementsText[i][0], acknowledgementsText[i][1]));
		}
		
		return createScrollableTextPanel("Acknowledgements", panelContainedWithinScrollPane);
	}
}
