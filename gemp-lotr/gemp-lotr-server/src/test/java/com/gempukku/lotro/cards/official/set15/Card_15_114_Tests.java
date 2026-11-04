package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_114_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("spear", "15_114");
					put("desolation", "15_107");  // Desolation Orc: an [orc] Orc; its only text is an extra cost to play (cheated in)
					put("overlord", "15_102");    // Black Land Overlord: an [orc] Orc of another title; a maneuver ability never used
					put("mtroll", "15_112");      // Mountain-troll: an [orc] Troll, no Orc; its texts are a discount to play and a Shadow ability never used
					put("troop", "1_177");        // Goblin Patrol Troop: a [moria] Orc with no text
					put("gollum", "5_24");        // Gollum, Nasty Treacherous Creature: a [gollum] Shadow card, which spots no Free Peoples culture; a regroup ability never used
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a [gondor] companion; a maneuver ability never used
					put("sam", "1_311");          // Sam, Son of Hamfast: a second [shire] companion beside Frodo
					put("orophin", "1_56");       // Orophin, Lórien Bowman: an [elven] ally; an archery ability never reached
					put("stairs", "1_24");        // Stairs of Khazad-dûm: a [dwarven] condition, no character; its text is about a Dwarf's skirmish
					put("gandalf", "1_72");       // Gandalf, Friend of the Shirefolk: a [gandalf] companion, played from hand
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrkishHuntingSpearStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Orkish Hunting Spear
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: +1
		 * Game Text: Bearer must be an [orc] Orc.<br>Bearer gains <b>hunter X</b> <helper>(While skirmishing a non-hunter character, this character is strength +X.)</helper>, where X is the number of Free Peoples cultures you can spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("spear");

		assertEquals("Orkish Hunting Spear", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void PlayedOnAnOrcOrcItGivesHunterForEachFreePeoplesCultureSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var spear = scn.GetShadowCard("spear");
		var desolation = scn.GetShadowCard("desolation");
		var overlord = scn.GetShadowCard("overlord");
		var mtroll = scn.GetShadowCard("mtroll");
		var troop = scn.GetShadowCard("troop");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCardsToHand(spear);
		assertInZone(Zone.HAND, spear);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(desolation, overlord, mtroll, troop, gollum);
		scn.ShadowRefreshDecision();

		assertEquals(0, scn.GetKeywordCount(desolation, Keyword.HUNTER));
		assertEquals(0, scn.GetKeywordCount(overlord, Keyword.HUNTER));
		assertTrue(scn.ShadowPlayAvailable(spear));

		scn.ShadowPlayCard(spear);
		assertTrue(scn.ShadowHasCardChoiceAvailable(desolation, overlord));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(mtroll, troop, gollum));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(desolation);
		assertAttachedTo(spear, desolation);
		// Frodo's [shire] and Aragorn's [gondor]; Gollum's [gollum] is a Shadow card's and spots no Free Peoples culture.
		assertEquals(2, scn.GetKeywordCount(desolation, Keyword.HUNTER));
		assertEquals(0, scn.GetKeywordCount(overlord, Keyword.HUNTER));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EveryKindOfFreePeoplesCardSpotsItsCultureOnceAndTheCountRisesAsOneIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var orophin = scn.GetFreepsCard("orophin");
		var stairs = scn.GetFreepsCard("stairs");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveAlliesToTable(orophin);
		scn.MoveCardsToSupportArea(stairs);
		scn.MoveCardsToHand(gandalf);

		var spear = scn.GetShadowCard("spear");
		var desolation = scn.GetShadowCard("desolation");
		var overlord = scn.GetShadowCard("overlord");
		var gollum = scn.GetShadowCard("gollum");

		scn.StartGame();
		// Laid in the fellowship phase to read the count as a companion is played (Q-0228).
		scn.MoveMinionsToTable(desolation, overlord, gollum);
		scn.AttachCardsTo(desolation, spear);
		scn.FreepsRefreshDecision();

		assertAttachedTo(spear, desolation);
		assertInZone(Zone.SUPPORT, stairs);
		// [shire] (Frodo and Sam, once), [gondor] (a companion), [elven] (an ally) and [dwarven] (a condition); not [gollum].
		assertEquals(4, scn.GetKeywordCount(desolation, Keyword.HUNTER));
		assertEquals(0, scn.GetKeywordCount(overlord, Keyword.HUNTER));

		scn.FreepsPlayCard(gandalf);
		assertEquals(5, scn.GetKeywordCount(desolation, Keyword.HUNTER));
		assertEquals(0, scn.GetKeywordCount(overlord, Keyword.HUNTER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
