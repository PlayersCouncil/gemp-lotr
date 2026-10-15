package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_254_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mordor", "1_254");

					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, vitality 2; a skirmish ability only
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, vitality 2; Damage +1 only
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, a minion of another culture; no text
					put("strength", "1_278");   // Strength Born of Fear: [sauron] support-area condition, a [sauron] card that is no minion; a skirmish ability only
					put("marksman", "1_176");   // Goblin Marksman: [moria] archer minion; Archer is its whole text

					put("legolas", "1_51");     // Legolas, Prince of Mirkwood: [elven] Elf archer companion; strength against a Nazgul only
					put("garrison", "8_36");    // Garrison of Gondor: [gondor] Man archer companion; defender only with a fortification
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no archer; a maneuver ability only
					put("orophin", "1_56");     // Orophin, Lorien Bowman: an archer ally; an archery ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MordorEnragedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Mordor Enraged
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype:
		 * Game Text: Plays on a [sauron] minion.<br><b>Shadow:</b> Exert bearer to add (1) for each archer companion.
		*/

		var scn = GetScenario();

		var mordor = scn.GetFreepsCard("mordor");

		assertEquals("Mordor Enraged", mordor.getBlueprint().getTitle());
		assertNull(mordor.getBlueprint().getSubtitle());
		assertFalse(mordor.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, mordor.getBlueprint().getSide());
		assertEquals(Culture.SAURON, mordor.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, mordor.getBlueprint().getCardType());
		assertEquals(0, mordor.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysOnASauronMinionOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mordor = scn.GetShadowCard("mordor");
		var soldier = scn.GetShadowCard("soldier");
		var sentry = scn.GetShadowCard("sentry");
		var troop = scn.GetShadowCard("troop");
		var strength = scn.GetShadowCard("strength");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(mordor);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(strength);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, sentry, troop);
		assertInHand(mordor);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(mordor));

		scn.ShadowPlayCard(mordor);
		// A [sauron] Orc and a [sauron] Uruk-hai; not a [moria] minion, a [sauron] card that is no minion, or a companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(soldier, sentry));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop, strength, aragorn, frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(sentry);

		assertAttachedTo(mordor, sentry);
		assertNotAttachedTo(mordor, soldier);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityExertsBearerToAddOneForEachArcherCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mordor = scn.GetShadowCard("mordor");
		var soldier = scn.GetShadowCard("soldier");
		var sentry = scn.GetShadowCard("sentry");
		var marksman = scn.GetShadowCard("marksman");
		var legolas = scn.GetFreepsCard("legolas");
		var garrison = scn.GetFreepsCard("garrison");
		var aragorn = scn.GetFreepsCard("aragorn");
		var orophin = scn.GetFreepsCard("orophin");
		// Two archer companions; the archer ally, the archer minion and Aragorn, a companion who is no archer, do not count.
		scn.MoveCompanionsToTable(legolas, garrison, aragorn);
		scn.MoveAlliesToTable(orophin);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, sentry, marksman);
		scn.AttachCardsTo(soldier, mordor);
		assertAttachedTo(mordor, soldier);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(6, scn.GetTwilight()); // 2 for Ettenmoors + 4 companions
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(sentry));
		assertTrue(scn.ShadowActionAvailable(mordor));

		scn.ShadowUseCardAction(mordor);

		assertEquals(1, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(sentry));
		assertEquals(0, scn.GetWoundsOn(marksman));
		assertEquals(8, scn.GetTwilight()); // 6 + 1 for each of the 2 archer companions
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithOneArcherCompanionAddsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mordor = scn.GetShadowCard("mordor");
		var soldier = scn.GetShadowCard("soldier");
		var marksman = scn.GetShadowCard("marksman");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var orophin = scn.GetFreepsCard("orophin");
		scn.MoveCompanionsToTable(legolas, aragorn);
		scn.MoveAlliesToTable(orophin);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, marksman);
		scn.AttachCardsTo(soldier, mordor);
		assertAttachedTo(mordor, soldier);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(5, scn.GetTwilight()); // 2 for Ettenmoors + 3 companions
		assertEquals(0, scn.GetWoundsOn(soldier));

		scn.ShadowUseCardAction(mordor);

		assertEquals(1, scn.GetWoundsOn(soldier));
		assertEquals(6, scn.GetTwilight()); // 5 + 1 for Legolas
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoArcherCompanionTheBearerExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mordor = scn.GetShadowCard("mordor");
		var soldier = scn.GetShadowCard("soldier");
		var marksman = scn.GetShadowCard("marksman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var orophin = scn.GetFreepsCard("orophin");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(orophin);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, marksman);
		scn.AttachCardsTo(soldier, mordor);
		assertAttachedTo(mordor, soldier);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(4, scn.GetTwilight()); // 2 for Ettenmoors + 2 companions
		assertEquals(0, scn.GetWoundsOn(soldier));
		// The ability has a cost, so it is offered though it will add nothing.
		assertTrue(scn.ShadowActionAvailable(mordor));

		scn.ShadowUseCardAction(mordor);

		assertEquals(1, scn.GetWoundsOn(soldier));
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWhileTheBearerIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mordor = scn.GetShadowCard("mordor");
		var soldier = scn.GetShadowCard("soldier");
		var sentry = scn.GetShadowCard("sentry");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, sentry);
		scn.AttachCardsTo(soldier, mordor);
		// Vitality 2 with 1 wound: the bearer is exhausted; the [sauron] minion beside him could exert.
		scn.AddWoundsToChar(soldier, 1);
		assertAttachedTo(mordor, soldier);
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertTrue(scn.IsExhausted(soldier));
		assertFalse(scn.IsExhausted(sentry));
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(mordor));
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(sentry));
	}
}
