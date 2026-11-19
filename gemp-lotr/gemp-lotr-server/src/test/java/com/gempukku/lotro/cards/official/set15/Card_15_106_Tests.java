package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_106_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("effort", "15_106");

					put("goblin", "11_137");     // Scurrying Goblin: [orc] Orc, cost 1; its trigger needs an underground site
					put("halftroll", "14_12");   // Half-troll of Far Harad: [orc] Half-troll, cost 3; its start-of-assignment trigger reaches no test here
					put("troop", "1_177");       // Goblin Patrol Troop: a [moria] Orc minion, no text
					put("beckoning", "12_83");   // The Beckoning Shadow: [orc] support-area condition; nothing below 5 burdens
					put("spear", "12_96");       // Orc Spear: an [orc] possession, cost 1; adds (2) only when played on a minion of cost 4 or more

					put("gimli", "1_13");        // Gimli, Son of Gloin: Dwarf; a skirmish ability only
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: a [gondor] Man and ranger; a maneuver ability only
					put("armor", "1_8");         // Dwarven Armor: a [dwarven] possession (armor) for a Dwarf, cost 0
					put("brand", "2_32");        // Flaming Brand: a [gondor] possession (hand weapon) for a Man, cost 0
					put("artifice", "17_4");     // Ring of Artifice: a Free Peoples artifact for a Dwarf, cost 0
					put("nostranger", "1_108");  // No Stranger to the Shadows: a Free Peoples condition for a ranger, cost 0; site Shadow numbers -1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CoordinatedEffortStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Coordinated Effort
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot 2 [orc] minions.
		 * Each time the Free Peoples player plays a possession, add (2).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("effort");

		assertEquals("Coordinated Effort", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilTwoOrcMinionsAreSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var effort = scn.GetShadowCard("effort");
		var goblin = scn.GetShadowCard("goblin");
		var halftroll = scn.GetShadowCard("halftroll");
		var troop = scn.GetShadowCard("troop");
		var beckoning = scn.GetShadowCard("beckoning");
		scn.MoveCardsToSupportArea(beckoning);
		scn.MoveCardsToHand(effort, halftroll);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, beckoning);
		assertInHand(effort, halftroll);
		scn.MoveMinionsToTable(goblin, troop);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// One [orc] minion, beside an Orc minion of another culture and an [orc] card that is no minion.
		assertInZone(Zone.SHADOW_CHARACTERS, goblin, troop);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(effort));

		// A second [orc] minion, of another race.
		scn.ShadowPlayCard(halftroll);
		assertInZone(Zone.SHADOW_CHARACTERS, halftroll);
		assertTrue(scn.ShadowPlayAvailable(effort));

		scn.ShadowPlayCard(effort);
		assertInZone(Zone.SUPPORT, effort);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachPossessionTheFreePeoplesPlayerPlaysAddsTwoAndAnArtifactOrAConditionAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var armor = scn.GetFreepsCard("armor");
		var brand = scn.GetFreepsCard("brand");
		var artifice = scn.GetFreepsCard("artifice");
		var nostranger = scn.GetFreepsCard("nostranger");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveCardsToHand(armor, brand, artifice, nostranger);

		var effort = scn.GetShadowCard("effort");
		scn.MoveCardsToSupportArea(effort);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, effort);
		assertInHand(armor, brand, artifice, nostranger);
		assertEquals(0, scn.GetTwilight());

		// Every card played here costs 0.
		scn.FreepsPlayCard(armor);
		assertAttachedTo(armor, gimli);
		assertEquals(2, scn.GetTwilight());

		scn.FreepsPlayCard(brand);
		assertAttachedTo(brand, aragorn);
		assertEquals(4, scn.GetTwilight());

		scn.FreepsPlayCard(artifice);
		assertAttachedTo(artifice, gimli);
		assertEquals(4, scn.GetTwilight());

		scn.FreepsPlayCard(nostranger);
		assertAttachedTo(nostranger, aragorn);
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void APossessionTheShadowPlayerPlaysAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var effort = scn.GetShadowCard("effort");
		var goblin = scn.GetShadowCard("goblin");
		var spear = scn.GetShadowCard("spear");
		scn.MoveCardsToSupportArea(effort);
		scn.MoveCardsToHand(spear);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, effort);
		assertInHand(spear);
		scn.MoveMinionsToTable(goblin);
		scn.SetTwilight(3);
		scn.FreepsPass();

		// 3 set + 1 companion + 2 for Ettenmoors.
		assertEquals(6, scn.GetTwilight());
		scn.ShadowPlayCard(spear);
		assertAttachedTo(spear, goblin);
		// 6 - 1 for the spear, and nothing added.
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
