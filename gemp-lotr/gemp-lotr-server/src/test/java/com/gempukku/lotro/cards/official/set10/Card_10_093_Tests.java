package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_093_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wretch", "10_93");

					put("hunter", "1_256");         // Morgul Hunter: a [sauron] Orc of vitality 4; strength +1 for each companion, nothing else
					put("troll", "10_101");         // Troll of Cirith Gorgor: a [sauron] Troll of vitality 4; damage +1, fierce, strength +1 for each possession
					put("troop", "1_177");          // Goblin Patrol Troop: a [moria] Orc of vitality 3, no text
					put("sentry", "10_84");         // Cirith Ungol Sentry: a [sauron] Uruk-hai of vitality 2 (can exert once, not twice), damage +1
					put("gollum", "5_24");          // Gollum, Nasty Treacherous Creature: a [gollum] Shadow card; a regroup ability only
					put("runner", "1_178");         // Goblin Runner: the card put on top of the Shadow draw deck, to be drawn

					put("aragorn", "1_89");         // Aragorn, Ranger of the North: [gondor] companion; a maneuver ability only ever used on purpose
					put("legolas", "1_50");         // Legolas, Greenleaf: [elven] companion; an archery ability only ever used on purpose
					put("sam", "1_311");            // Sam, Son of Hamfast: [shire] companion; his abilities are used only on purpose
					put("stairs", "1_24");          // Stairs of Khazad-dum: [dwarven] support-area condition, the only [dwarven] card
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MordorWretchStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Mordor Wretch
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 6
		 * Game Text: <b>Shadow:</b> Spot 4 Free Peoples cultures and exert another [sauron] minion twice to add (3) and draw a card.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("wretch");

		assertEquals("Mordor Wretch", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithFourCulturesItExertsAnotherSauronMinionTwiceToAddThreeAndDrawACard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wretch = scn.GetShadowCard("wretch");
		var hunter = scn.GetShadowCard("hunter");
		var troll = scn.GetShadowCard("troll");
		var troop = scn.GetShadowCard("troop");
		var sentry = scn.GetShadowCard("sentry");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var stairs = scn.GetFreepsCard("stairs");
		scn.MoveCompanionsToTable(aragorn, legolas);
		scn.MoveCardsToSupportArea(stairs);
		assertInZone(Zone.SUPPORT, stairs);

		scn.StartGame();
		scn.MoveMinionsToTable(wretch, hunter, troll, troop, sentry);
		scn.MoveCardsToTopOfDeck(runner);
		assertInZone(Zone.SHADOW_CHARACTERS, wretch, hunter, troll, troop, sentry);
		assertInZone(Zone.DECK, runner);
		assertEquals(runner, scn.GetShadowTopOfDeck());
		scn.FreepsPass();

		// Exactly 4: [shire] Frodo, [gondor] Aragorn, [elven] Legolas, and [dwarven] by a condition alone.
		assertEquals(4, scn.GetFreepsCultureCount());
		// 3 companions + 2 for the site
		assertEquals(5, scn.GetTwilight());
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(0, scn.GetWoundsOn(troll));
		assertTrue(scn.ShadowActionAvailable(wretch));

		scn.ShadowUseCardAction(wretch);
		// [sauron] minions of two races who can exert twice; not the Wretch himself, a [moria] Orc, or a
		// [sauron] Uruk-hai who can exert only once.
		assertTrue(scn.ShadowHasCardChoiceAvailable(hunter, troll));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(wretch, troop, sentry));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(troll);

		assertEquals(2, scn.GetWoundsOn(troll));
		assertEquals(0, scn.GetWoundsOn(hunter));
		assertEquals(0, scn.GetWoundsOn(wretch));
		assertEquals(8, scn.GetTwilight());
		assertEquals(1, scn.GetShadowHandCount());
		assertInHand(runner);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithThreeCulturesBesideAGollumCardAndASecondShireCardItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wretch = scn.GetShadowCard("wretch");
		var hunter = scn.GetShadowCard("hunter");
		var gollum = scn.GetShadowCard("gollum");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, legolas, sam);

		scn.StartGame();
		scn.MoveMinionsToTable(wretch, hunter, gollum);
		assertInZone(Zone.SHADOW_CHARACTERS, wretch, hunter, gollum);
		scn.FreepsPass();

		// [shire] Frodo and Sam, [gondor] Aragorn, [elven] Legolas; a [gollum] Shadow card spots no Free Peoples culture.
		assertEquals(3, scn.GetFreepsCultureCount());
		assertEquals(0, scn.GetWoundsOn(hunter));
		assertFalse(scn.ShadowActionAvailable(wretch));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithTheOnlyOtherSauronMinionAbleToExertOnceItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wretch = scn.GetShadowCard("wretch");
		var hunter = scn.GetShadowCard("hunter");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var stairs = scn.GetFreepsCard("stairs");
		scn.MoveCompanionsToTable(aragorn, legolas);
		scn.MoveCardsToSupportArea(stairs);
		assertInZone(Zone.SUPPORT, stairs);

		scn.StartGame();
		scn.MoveMinionsToTable(wretch, hunter);
		// Vitality 4 with 2 wounds: 2 left, one exertion and not two.  The Wretch himself could exert twice.
		scn.AddWoundsToChar(hunter, 2);
		assertEquals(2, scn.GetWoundsOn(hunter));
		assertEquals(0, scn.GetWoundsOn(wretch));
		scn.FreepsPass();

		assertEquals(4, scn.GetFreepsCultureCount());
		assertEquals(2, scn.GetVitality(hunter));
		assertFalse(scn.ShadowActionAvailable(wretch));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
