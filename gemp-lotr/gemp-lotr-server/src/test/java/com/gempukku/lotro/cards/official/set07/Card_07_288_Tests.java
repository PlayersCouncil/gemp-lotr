package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_288_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("regular", "7_288");
					put("regular2", "7_288");
					// Cirith Ungol Guard: [sauron] Uruk-hai, a [sauron] minion who is no Orc; Damage +1 is its whole text
					put("uruk", "10_81");
					put("morgul", "7_199");     // Morgul Soldier: a [wraith] Orc; Nazgul are strength +1 (none in play)
					// Strength Born of Fear: [sauron] support-area condition, a [sauron] card that is no minion; its
					// skirmish ability costs (3) and the pool is empty
					put("fear", "1_278");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MordorRegularStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Mordor Regular
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 6
		 * Game Text: <b>Skirmish:</b> Exert this minion and spot another [sauron] minion to add a threat.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("regular");

		assertEquals("Mordor Regular", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void SkirmishAbilityExertsThisMinionToAddAThreatWithASauronUrukHaiToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var regular = scn.GetShadowCard("regular");
		var uruk = scn.GetShadowCard("uruk");

		scn.StartGame();
		scn.MoveMinionsToTable(regular, uruk);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, regular);
		// The Uruk-hai is left in no skirmish: spotting asks nothing of where he stands.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Two companions, so a second threat would show.
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(regular));
		assertEquals(0, scn.GetWoundsOn(uruk));
		assertTrue(scn.ShadowActionAvailable(regular));

		scn.ShadowUseCardAction(regular);

		assertEquals(1, scn.GetWoundsOn(regular));
		assertEquals(0, scn.GetWoundsOn(uruk));
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ASecondMordorRegularInTheSameSkirmishIsAnotherSauronMinionAndIsNotExerted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var regular = scn.GetShadowCard("regular");
		var regular2 = scn.GetShadowCard("regular2");

		scn.StartGame();
		scn.MoveMinionsToTable(regular, regular2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, regular);
		scn.ShadowAssignToMinions(aragorn, regular2);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(regular));
		assertEquals(0, scn.GetWoundsOn(regular2));
		assertTrue(scn.ShadowActionAvailable(regular));

		scn.ShadowUseCardAction(regular);

		assertEquals(1, scn.GetWoundsOn(regular));
		assertEquals(0, scn.GetWoundsOn(regular2));
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWithOnlyASauronConditionAndAWraithOrcBesideIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var regular = scn.GetShadowCard("regular");
		var morgul = scn.GetShadowCard("morgul");
		var fear = scn.GetShadowCard("fear");

		scn.StartGame();
		scn.MoveMinionsToTable(regular, morgul);
		scn.MoveCardsToSupportArea(fear);
		assertInZone(Zone.SUPPORT, fear);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, regular);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Regular can exert; "another" leaves Regular himself out, and neither the condition nor the [wraith]
		// Orc is a [sauron] minion.
		assertInZone(Zone.SUPPORT, fear);
		assertInZone(Zone.SHADOW_CHARACTERS, morgul);
		assertEquals(0, scn.GetWoundsOn(regular));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(regular));
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWhileThisMinionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var regular = scn.GetShadowCard("regular");
		var uruk = scn.GetShadowCard("uruk");

		scn.StartGame();
		scn.MoveMinionsToTable(regular, uruk);
		// Vitality 2 with 1 wound: exhausted.
		scn.AddWoundsToChar(regular, 1);
		assertEquals(1, scn.GetWoundsOn(regular));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, regular);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// The Uruk-hai beside him could exert, but the cost is this minion's own.
		assertEquals(0, scn.GetWoundsOn(uruk));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(regular));
	}
}
