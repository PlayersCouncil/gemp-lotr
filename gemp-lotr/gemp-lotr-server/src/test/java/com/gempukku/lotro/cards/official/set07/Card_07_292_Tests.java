package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_07_292_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("veteran", "7_292");
					// Cirith Ungol Sentry: [sauron] Uruk-hai, a [sauron] minion who is no Orc; Damage +1 is its whole text
					put("sentry", "10_84");
					// Morgul Hound: [wraith] Orc, strength 5; +4 and fierce only at 2 wounds on the Ring-bearer, who has none here
					put("hound", "7_192");
					// Strength Born of Fear: [sauron] support-area condition, a [sauron] card that is no minion; its
					// skirmish ability costs (3) and the pool is empty
					put("fear", "1_278");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability used only on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: strength 7, vitality 3; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MordorVeteranStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Mordor Veteran
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 6
		 * Game Text: <b>Skirmish:</b> Remove 3 threats to make a [sauron] minion strength +8.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("veteran");

		assertEquals("Mordor Veteran", card.getBlueprint().getTitle());
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
	public void SkirmishAbilityRemovesThreeThreatsToMakeASauronMinionStrengthPlus8ForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var veteran = scn.GetShadowCard("veteran");
		var sentry = scn.GetShadowCard("sentry");
		var hound = scn.GetShadowCard("hound");
		var fear = scn.GetShadowCard("fear");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, gimli, boromir);
		scn.MoveCardsToSupportArea(fear);

		scn.StartGame();
		// Frodo, Aragorn, Gimli and Boromir: room for the four threats.
		scn.AddThreats(4);
		assertEquals(4, scn.GetThreats());
		assertInZone(Zone.SUPPORT, fear);
		scn.MoveMinionsToTable(veteran, sentry, hound);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, veteran}, new PhysicalCardImpl[]{gimli, hound});
		// The Sentry stays out of every skirmish.
		scn.ShadowDeclineAssignments();

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertEquals(9, scn.GetStrength(veteran));
		assertEquals(8, scn.GetStrength(sentry));
		assertTrue(scn.ShadowActionAvailable(veteran));

		scn.ShadowUseCardAction(veteran);
		// The Veteran (an Orc) in his skirmish and the Sentry (an Uruk-hai) in none; not a [sauron] card that is no
		// minion, nor a minion of another culture.
		assertTrue(scn.ShadowHasCardChoiceAvailable(veteran, sentry));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(fear, hound));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(sentry);

		assertEquals(1, scn.GetThreats());
		assertEquals(16, scn.GetStrength(sentry));
		assertEquals(9, scn.GetStrength(veteran));
		assertEquals(5, scn.GetStrength(hound));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.PassSkirmishActions();
		// The Veteran's 9 against Aragorn's 8: a wound, not a kill.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.FreepsResolveSkirmish(gimli);

		// The +8 was made in Aragorn's skirmish and is gone in the next.
		assertEquals(8, scn.GetStrength(sentry));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWithTwoThreats() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var veteran = scn.GetShadowCard("veteran");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreats());
		scn.MoveMinionsToTable(veteran);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, veteran);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// One threat short of the cost.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(veteran));
		assertEquals(2, scn.GetThreats());
		assertEquals(9, scn.GetStrength(veteran));
	}
}
