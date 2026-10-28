package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_291_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("trooper", "7_291");
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, strength 7, vitality 2; its text answers a weapon played on it
					// Cirith Ungol Sentry: [sauron] Uruk-hai, a [sauron] minion who is no Orc; Damage +1 is its whole text
					put("sentry", "10_84");
					// Morgul Hound: [wraith] Orc, strength 5; +4 and fierce only at 2 wounds on the Ring-bearer, who has none here
					put("hound", "7_192");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MordorTrooperStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Mordor Trooper
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 6
		 * Vitality: 2
		 * Site Number: 6
		 * Game Text: <b>Skirmish:</b> Remove 2 threats to make a [sauron] Orc strength +6.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("trooper");

		assertEquals("Mordor Trooper", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void SkirmishAbilityRemovesTwoThreatsToMakeASauronOrcStrengthPlus6ForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trooper = scn.GetShadowCard("trooper");
		var chieftain = scn.GetShadowCard("chieftain");
		var sentry = scn.GetShadowCard("sentry");
		var hound = scn.GetShadowCard("hound");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		// Frodo, Aragorn and Gimli: room for the three threats.
		scn.AddThreats(3);
		assertEquals(3, scn.GetThreats());
		scn.MoveMinionsToTable(trooper, chieftain, sentry, hound);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, trooper}, new PhysicalCardImpl[]{gimli, hound});
		// The Chieftain and the Sentry stay out of every skirmish.
		scn.ShadowDeclineAssignments();

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertEquals(6, scn.GetStrength(trooper));
		assertEquals(7, scn.GetStrength(chieftain));
		assertTrue(scn.ShadowActionAvailable(trooper));

		scn.ShadowUseCardAction(trooper);
		// The Trooper in his skirmish and the Chieftain in none; not a [sauron] Uruk-hai nor a [wraith] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(trooper, chieftain));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sentry, hound));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(chieftain);

		assertEquals(1, scn.GetThreats());
		assertEquals(13, scn.GetStrength(chieftain));
		assertEquals(6, scn.GetStrength(trooper));
		assertEquals(8, scn.GetStrength(sentry));
		assertEquals(5, scn.GetStrength(hound));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		assertEquals(0, scn.GetWoundsOn(trooper));
		scn.PassSkirmishActions();
		// The Trooper's 6 against Aragorn's 8: the Trooper takes the wound.
		assertEquals(1, scn.GetWoundsOn(trooper));
		scn.FreepsResolveSkirmish(gimli);

		// The +6 was made in Aragorn's skirmish and is gone in the next.
		assertEquals(7, scn.GetStrength(chieftain));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityMakesTheTrooperHimselfStrongerAndIsNotOfferedWithOneThreatLeft() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trooper = scn.GetShadowCard("trooper");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.AddThreats(3);
		assertEquals(3, scn.GetThreats());
		scn.MoveMinionsToTable(trooper);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, trooper);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(6, scn.GetStrength(trooper));
		assertTrue(scn.ShadowActionAvailable(trooper));
		scn.ShadowUseCardAction(trooper);
		// The one [sauron] Orc in play is chosen with nothing asked.
		assertEquals(1, scn.GetThreats());
		assertEquals(12, scn.GetStrength(trooper));

		// One threat left, one short of the cost.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(trooper));
		assertEquals(1, scn.GetThreats());
		assertEquals(12, scn.GetStrength(trooper));
	}
}
