package guiPieces;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import modelPieces.DoTInformation;
import modelPieces.UtilityInformation;
import utilities.MathUtils;

public class InformationTabsText {

	private static JPanel createScrollableTextPanel(String title, JPanel scrollableText) {
		 JPanel toReturn = new JPanel();
		 toReturn.setLayout(new BorderLayout());
		 toReturn.setBackground(GuiConstants.drgBackgroundBrown);
		 
		 JLabel header = new JLabel(title);
		 header.setForeground(GuiConstants.drgRegularOrange);
		 header.setFont(new Font("Dialogue", Font.PLAIN, 18));
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
		questionOrTerm.setForeground(GuiConstants.drgRegularOrange);
		// Set the Label to be almost flush with the left side
		JPanel leftAlignLabel = new JPanel();
		leftAlignLabel.setLayout(new FlowLayout(FlowLayout.LEFT));
		leftAlignLabel.setBackground(GuiConstants.drgBackgroundBrown);
		leftAlignLabel.add(questionOrTerm);
		toReturn.add(leftAlignLabel);
		
		JTextArea answerOrDefinition = new JTextArea(answer);
		answerOrDefinition.setBackground(GuiConstants.drgBackgroundBrown);
		answerOrDefinition.setForeground(GuiConstants.drgHighlightedYellow);
		// Left-pad the answer a bit for visual clarity
		answerOrDefinition.setMargin(new Insets(0, 30, 8, 0));
		answerOrDefinition.setWrapStyleWord(true);
		answerOrDefinition.setLineWrap(true);
		toReturn.add(answerOrDefinition);
		
		return toReturn;
	}
	
	public static JPanel getFAQText() {
		// TODO: add more FAQs
		String[][] FAQtext = {
			{"Where are the Flamethrower, Cryo Cannon, and Breach Cutter?", "Those weapons (Breach Cutter in particular) are substantially harder to accurately model, but I'm hoping to add them in a later release."},
			{"Why do some Mods and Overclocks have a Red outline?", "Mods or Overclocks with a Red outline either are not implemented yet, or how they work in-game can't be represented by the Weapon's stats."},
			{"What's the point of this program?", "To help the DRG community compare and contrast their preferred builds for each weapon, and to provide more detail about how the weapons work than described in-game or on the wiki."},
			{"I think something is wrong/missing, how do I communicate that to you?", "In the 'Misc. Actions' Menu, there's an option to suggest changes. That should automatically open up this project's GitHub issue creation page for you."},
			{"Can I help improve to this project?", "Yes! This is an open-source, freeware fan project. Although it's started out as just one developer, I would love to have help."},
			{"How frequently will this be updated?", "There are a couple features that I want to add (like the 3 missing weapons) before calling this 'stable', but I'm planning to update each weapon's stats as GSG devs update them in-game on their production build."},
			{"Will this be made available as a live website?", "Probably not. Thousands of lines of Java code do not port well into HTML/CSS/Javascript. There's a similar program already online at https://surmiran.github.io/karl/ but it has much less detail."},
			{"How did you model [insert mechanic here]?", "This is an open-source project. Feel free to look around the source code and see how it was done. In general though: I chose to model everything like a continuous function instead of "
					+ "discrete. Slight loss of accuracy, but significantly easier."},
			{"How are Status Effect Utility scores calculated?", "The formula I chose to use is (% Chance to Proc) * (Number of Targets) * (Effect Duration) * (Utility Factor), where 'Utility Factor' is some scalar value assigned to each effect."},
			{"Why do some of the Base Spread, Spread per Shot, and Recoil values in your program not match what's listed in-game or in the wiki?", "Simply put: because they're either wrong or incomplete. I did over 20 hours of data collection "
					+ "and analysis to model the accuracy of the projectile weapons as precisely as I could."},
			// I'm intentionally adding blank lines below here so that the content gets pushed to the top of the page
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
			{"Armor", "Some of the enemies on Hoxxes IV have exterior armor plates to protect them. Grunts have armor that reduces damage by 20%, but has a chance to break every time it gets damaged. Praetorians and "
					+ "Shellbacks have armor plates that negate all incoming Direct Damage, but break after absorbing 100 total damage. The third type of armor is found on Oppressors and Dreadnoughts: it makes them "
					+ "immune to all Direct Damage from the front and can't be broken, so shoot their abdomen."},
			{"Armor Breaking", "Increasing this stat above 100% means that it takes fewer shots to break Grunt and Praetorian armor plates, so you lose less damage to Armor. Doesn't affect the third type of Armor, though. "
					+ "Likewise, if this is less than 100%, it means that damage is less effective vs Armor."},
			{"Weakpoint", "Most enemies have certain spots on their body that will take extra Direct Damage. Those spots are referred to as Weakpoints. Common areas are the mouths of medium-sized Glyphids, the abdomens of "
					+ "Macteras, Praetorians, and Dreadnoughts, and glowing bulbs on the sides of larger enemies like Bulk Detonators, Wardens, and Menaces. With the exception of mouths, weakpoints are usually brightly colored and will light up when damaged."},
			{"Weakpoint Bonus", "Some weapons that deal Direct Damage have Mods or Overclocks that affect how that damage gets multiplied when impacting a Weakpoint. Most of the time, it's a multiplicative bonus that gets applied to the projectile before the Weakpoint's multiplier gets used."},
			{"Direct Damage", "Also known as 'Kinetic Damage', this is the primary damage type of guns that shoot bullets. If something that deals Direct Damage hits an enemy's weakpoint, then the Direct Damage gets increased by that weakpoint's multiplier, "
					+ "as well as any innate Weakpoint Bonus Damage from the weapon that fired it. Direct Damage gets reduced by Armor until the armor plate breaks, and does triple damage to Frozen enemies."},
			{"Area Damage", "Also known as 'Explosive Damage', this damage type is found almost exclusively on things that explode. Engie's Grenade Launcher, Gunner's Autocannon, most of the throwable grenades, etc. Because this damage gets applied to all enemies "
					+ "within an Area of Effect (AoE), it effectively bypasses Armor. Does not benefit when impacting weakpoints or Frozen targets."},
			{"Fire Damage", "One of the elemental damage types, Fire Damage is similar to Direct Damage. The primary difference is that certain enemies have a resistance or weakness to Fire Damage, which affects how much health that enemy loses due to Fire Damage."},
			{"Heat Damage", "Heat Damage doesn't actually affect an enemy's healthbar; rather it affects their Temperature. Once enough Heat has been accumulated, the enemy receives a Burn DoT which continues until the enemy "
					+ "sheds enough Heat to be doused, or the enemy dies. Applying more Heat Damage to an already ignited enemy keeps their temperature at maximum, and prolongs the Burn duration. Heat Damage counteracts Cold Damage."},
			{"Frost Damage", "The second elemental damage type, Frost Damage is the inverse of Fire Damage. It also affects enemy healthbars, some enemies resist it, and some are weak to it."},
			{"Cold Damage", "As Frost Damage is to Fire, so Cold is to Heat. Applying Cold Damage to an enemy decreases their Temperature until eventually they become Frozen. Enemies remain Frozen until they gain enough Heat to thaw, "
					+ "at which point they can start accumulating Cold Damage again. Cold Damage counteracts Heat Damage."},
			{"Electric Damage", "The third elemental damage types, Electric Damage affects enemies' healthbars. Just like Fire and Frost Damage, Electric is resisted by certain enemies and extra effective versus others."},
			{"Poison Damage", "The last of the elemental damage types, Poison Damage is typically dealt by enemies or the environment. However, some weapons are able to use this damage by applying a Neurotoxin DoT to enemies."},
			{"DoT", "An acronym that stands for 'Damage Over Time'. This term is used to refer to damage which doesn't get applied per projectile, but rather gets applied over a period of time."},
			{"Status Effect", "A conditional effect that can be applied to enemies. Sometimes it's a DoT, other times it's a crowd control effect."},
			{"Burn (DoT)", "When an enemy has its Temperature meter increased to maximum by taking sustained Heat Damage, it ignites and gains a Burn DoT. While Burning, enemies take an average of " + MathUtils.round(DoTInformation.Burn_DPS, GuiConstants.numDecimalPlaces) 
					+  " Fire Damage per second. If no more Heat Damage is applied, then their Temperature will steadily decrease until they are doused and the Burn DoT will end. On the other hand, sustaining even more Heat Damage will prolong the Burn duration. Applying "
					+ "Cold Damage will significantly shorten the duration of the Burn."},
			{"Frozen (Status Effect)", "Thematically the opposite of the Burn DoT, enemies become Frozen when their Temperature is lowered enough by sustained Cold Damage. Once Frozen, they receive x" + UtilityInformation.Frozen_Damage_Multiplier + " Direct Damage. Frozen enemies "
					+ "cannot have the freeze duration increased; instead they will thaw over time. Once they have thawed, more Cold Damage can be applied to freeze them again. Applying Heat Damage will significantly shorten the duration of the Freeze."},
			{"Electrocute (DoT, Status Effect)", "Some of the Weapons and Overclocks have a chance to apply the Electrocute Status Effect. Once applied, enemies take an average of " + MathUtils.round(DoTInformation.Electro_DPS, GuiConstants.numDecimalPlaces) + " Electric Damage per second for " 
					+ DoTInformation.Electro_SecsDuration + " seconds, while also being slowed by 80%. Enemies can only have one Electrocute applied to them at once; if another shot were to apply a second Electrocute, the first one has its duration refreshed instead."},
			{"Radiation (DoT)", "There are two types of Radiation: environmental hazards in the Radioactive Exclusion Zone which deal an average of " + MathUtils.round(DoTInformation.Rad_Env_DPS, GuiConstants.numDecimalPlaces) + " Radiation Damage per second to the player, and the Radiation field left behind by the "
					+ "Overclock 'Fat Boy', which does an average of " + MathUtils.round(DoTInformation.Rad_FB_DPS, GuiConstants.numDecimalPlaces) + " Radiation Damage per second to enemies."},
			{"Neurotoxin (DoT, Status Effect)", "Similar to Electrocute, a few weapons can have a chance to apply Neurotoxin. Enemies afflicted by Neurotoxin take an average of " + MathUtils.round(DoTInformation.Neuro_DPS, GuiConstants.numDecimalPlaces) + " Poison Damage per second for up to " 
					+ DoTInformation.Neuro_SecsDuration + " seconds, while also being slowed by 30%. Also like Electrocute, enemies can only have one Neurotoxin DoT applied to them at once; anything that would apply a second effect instead refreshes the duration."},
			{"Stun (Status Effect)", "Stunning an enemy stops them from moving or attacking for a set duration. That duration changes from weapon to weapon, but it's typically around 2 seconds. Enemies that channel their attacks (like Praetorians) can have those attacks interrurpted by a Stun."},
			{"Fear (Status Effect)", "Inflicting Fear on an enemy causes them to stop what they're doing and run from the source of the Fear as fast as they can move for about " + UtilityInformation.Fear_Duration + " seconds. After the Fear wears off, they return to normal behavior."},
			{"Base Spread", "This stat affects how accurate the first shot will be. At 0%, that means the first shot is guaranteed to go exactly where your crosshair is pointing. As the percentage goes higher, the probability that the first shot will hit decreases."},
			{"Spread Per Shot", "After every shot gets fired, the maximum area of the crosshair increases by this amount. Thus, successive shots get increasingly less likely to hit your intended target until it reaches Max Spread."},
			{"Spread Recovery", "This stat is constantly reducing the current Spread of the gun, trying to return to Base Spread. Because this is a constant rate, it's more effective the lower the Rate of Fire."},
			{"Recoil", "Recoil is an estimate of how far off-axis the center of the Spread is after each shot. Typically, Recoil is primarily a vertical climb with a little horizontal movement, but some weapons have significantly more hotizontal movement than others. "
					+ "While Spread has 4 different pieces (Base, Per Shot, Max, and Recovery), all 3 parts of Recoil get affected by the same percentage increase/decrease such that the time to recover from Max Recoil is the same for any percentage."},
			{"Time to Kill (TTK)", "As the name implies, this metric is used to estimate how quickly a weapon can kill an enemy. Because there are so many enemy types with different healthbars and spawn rates, this metric gets evaluated as the weighted average of all "
					+ "healthbars divided by the Sustained Weakpoint DPS of the weapon."},
			{"Overkill", "Because enemy healthbars don't normally come in multiples of the damage done by each Weapon, there's inevitably some damage going to be wasted. Overkill is an approximation of how much damage gets wasted by a Weapon as it kills the weighted average healthbar."},
			{"Mobility", "Some Mods or Overclocks can affect how efficiently players move around the environment. Often, they are conditional increases or decreases to movement speed, but sometimes they provide the ability to 'Blast Jump'."},
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
			{"Ian McDonagh", "Thank you for creating the open-source JAR 'image4j' that allows me to use .ico files natively."},
			{"Gaming for the Recently Deceased", "Thank you for helping to promote this project and making a video about it. YouTube Channel: https://www.youtube.com/channel/UCL_8gMChYJD5ls7GaJtGmUw"},
			{"Usteppin", "Thank you for collect some data for me on Hazard 5. Twitch Channel: https://www.twitch.tv/usteppin"},
			{"Minomess", "Thank you for being the first Alpha tester and giving a lot of helpful feedback."},
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
