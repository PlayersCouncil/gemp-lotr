package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_083_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("enquea", "2_83");

					// Úlairë Lemenya, Lieutenant of Morgul: another Nazgûl, left unassigned; her text only weakens
					// characters bearing a [wraith] condition.
					put("lemenya", "1_232");
					// Fell Beast: "Bearer must be a Nazgûl. Bearer is fierce." Strength +2.
					put("fellbeast", "6_83");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4, so 11 (or 13) beats him without overwhelming him
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound and not the Ring-bearer; his response answers only Frodo's death
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				// The Great Ring answers no wound while it is not worn, and adds no strength.
				VirtualTableScenario.GreatRing
		);
	}

	@Test
	public void UlaireEnqueaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Úlairë Enquëa, Ringwraith in Twilight
		 * Unique: True
		 * Side: Shadow
		 * Culture: Ringwraith
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 11
		 * Vitality: 4
		 * Site Number: 3
		 * Game Text: <b>Twilight</b>.<br>Each time Úlairë Enquëa wins a skirmish, you may exert him to wound the Ring-bearer once (or twice if you can spot 5 burdens).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("enquea");

		assertEquals("Úlairë Enquëa", card.getBlueprint().getTitle());
		assertEquals("Ringwraith in Twilight", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.TWILIGHT));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(11, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhenHeWinsHeMayExertToWoundTheRingBearerOnceWithFourBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);

		var enquea = scn.GetShadowCard("enquea");
		var lemenya = scn.GetShadowCard("lemenya");

		scn.StartGame();
		scn.AddBurdens(4);
		assertEquals(4, scn.GetBurdens());
		scn.MoveMinionsToTable(enquea, lemenya);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, enquea);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// 11 against 8: he wins.
		assertEquals(4, scn.GetBurdens());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(enquea));
		assertEquals(0, scn.GetWoundsOn(enquea));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		scn.ShadowAcceptOptionalTrigger();

		// He exerts himself, not the other Nazgûl beside him.
		assertEquals(1, scn.GetWoundsOn(enquea));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		// Four burdens: once.  The Ring-bearer, not Sam, who is Ring-bound.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(4, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithFiveBurdensTheRingBearerIsWoundedTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var enquea = scn.GetShadowCard("enquea");

		scn.StartGame();
		scn.AddBurdens(5);
		assertEquals(5, scn.GetBurdens());
		scn.MoveMinionsToTable(enquea);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, enquea);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertEquals(5, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(enquea));
		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(enquea));
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(5, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheWoundKillsAnExhaustedRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var enquea = scn.GetShadowCard("enquea");

		scn.StartGame();
		scn.AddBurdens(4);
		assertEquals(4, scn.GetBurdens());
		scn.MoveMinionsToTable(enquea);
		// Vitality 4 with 3 wounds: Frodo could not exert, but a wound kills him.
		scn.AddWoundsToChar(frodo, 3);
		assertEquals(3, scn.GetWoundsOn(frodo));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, enquea);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertEquals(3, scn.GetWoundsOn(frodo));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(enquea));
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.GameIsFinished());
		assertTrue(scn.FreepsHasLost());
	}

	@Test
	public void NothingIsOfferedWhenHeLosesASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		// 18 against 11: Aragorn wins, and Enquëa lives through the wound.
		scn.ApplyAdHocModifier(new StrengthModifier(null, Filters.name("Aragorn"), null, 10));

		var enquea = scn.GetShadowCard("enquea");

		scn.StartGame();
		scn.MoveMinionsToTable(enquea);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, enquea);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(enquea));
		assertInPlay(enquea);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(enquea));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenHeIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var enquea = scn.GetShadowCard("enquea");

		scn.StartGame();
		scn.MoveMinionsToTable(enquea);
		// Vitality 4 with 3 wounds: he cannot exert.
		scn.AddWoundsToChar(enquea, 3);
		assertEquals(3, scn.GetWoundsOn(enquea));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, enquea);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(3, scn.GetWoundsOn(enquea));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(enquea));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EachWinIsOfferedAgainAndDecliningChangesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var enquea = scn.GetShadowCard("enquea");
		var fellbeast = scn.GetShadowCard("fellbeast");

		scn.StartGame();
		scn.MoveMinionsToTable(enquea);
		// Fierce, so that he skirmishes, and wins, a second time in the turn: 13 against 8.
		scn.AttachCardsTo(enquea, fellbeast);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, enquea);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(enquea));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(enquea));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(aragorn));

		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, enquea);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassFierceSkirmishActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(enquea));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(enquea));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
