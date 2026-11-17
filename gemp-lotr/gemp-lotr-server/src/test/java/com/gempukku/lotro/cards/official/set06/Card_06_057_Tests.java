package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_06_057_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("agents", "6_57");
					put("agents2", "6_57");     // a second copy: the card is not unique and its text says "this minion"
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5; damage +1 and nothing else
					put("gollum", "5_25");      // Gollum, Stinker: a [gollum] Shadow card, which shows no Free Peoples culture

					// The Ring-bearer, Frodo 1_290, is the [shire] culture.
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] companion; a maneuver ability used only on purpose
					put("boromir", "1_96");     // Boromir, Lord of Gondor: a second [gondor] companion
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] companion; a skirmish ability used only on purpose
					put("smeagol", "6_45");     // Smeagol, Poor Creature: a [gollum] companion, a Free Peoples card; to play, add a burden
					put("cart", "1_73");        // Gandalf's Cart: a [gandalf] possession in the support area, no character; a fellowship ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AgentsofOrthancStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Agents of Orthanc
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: This minion is strength +2 and <b>damage +1</b> for each Free Peoples culture that you can spot over 2.
		*/

		var scn = GetScenario();

		var card = scn.GetShadowCard("agents");

		assertEquals("Agents of Orthanc", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsStrengthPlus2AndDamagePlus1ForEachFreePeoplesCultureOver2AsCompanionsArePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCardsToHand(aragorn, gimli, smeagol);

		var agents = scn.GetShadowCard("agents");
		var agents2 = scn.GetShadowCard("agents2");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		assertInHand(aragorn, gimli, smeagol);
		// The minions stand on the table while the fellowship is played out (a shortening, Q-0228).
		scn.MoveMinionsToTable(agents, agents2, savage);
		scn.FreepsRefreshDecision();

		// Frodo's [shire] alone: one culture, none over 2, and nothing taken off the printed numbers.
		assertEquals(9, scn.GetStrength(agents));
		assertEquals(0, scn.GetKeywordCount(agents, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(agents, Keyword.DAMAGE));

		// [gondor]: two cultures, still none over 2.
		scn.FreepsPlayCard(aragorn);
		assertEquals(9, scn.GetStrength(agents));
		assertEquals(0, scn.GetKeywordCount(agents, Keyword.DAMAGE));

		// [dwarven]: one over 2.
		scn.FreepsPlayCard(gimli);
		assertEquals(11, scn.GetStrength(agents));
		assertEquals(1, scn.GetKeywordCount(agents, Keyword.DAMAGE));

		// [gollum], on a Free Peoples card: two over 2.
		scn.FreepsPlayCard(smeagol);
		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertEquals(13, scn.GetStrength(agents));
		assertEquals(2, scn.GetKeywordCount(agents, Keyword.DAMAGE));
		// The second copy counts the same cultures for itself, once (not +8 for two copies); another minion gets nothing.
		assertEquals(13, scn.GetStrength(agents2));
		assertEquals(2, scn.GetKeywordCount(agents2, Keyword.DAMAGE));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(1, scn.GetKeywordCount(savage, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ACultureShownByAPossessionCountsAndASecondGondorCompanionOrAGollumMinionCountsNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cart = scn.GetFreepsCard("cart");
		scn.MoveCompanionsToTable("aragorn", "boromir", "gimli");
		scn.MoveCardsToSupportArea(cart);

		var agents = scn.GetShadowCard("agents");
		var gollum = scn.GetShadowCard("gollum");

		scn.StartGame();
		assertInZone(Zone.SUPPORT, cart);
		scn.MoveMinionsToTable(agents, gollum);

		// [shire], [gondor], [dwarven] and the Cart's [gandalf]: two over 2, so +4 and damage +2.  Boromir's [gondor] is
		// counted once with Aragorn's, and Gollum's [gollum] is no Free Peoples culture: either counted would read 15
		// and damage +3.
		assertEquals(13, scn.GetStrength(agents));
		assertEquals(2, scn.GetKeywordCount(agents, Keyword.DAMAGE));
	}
}
