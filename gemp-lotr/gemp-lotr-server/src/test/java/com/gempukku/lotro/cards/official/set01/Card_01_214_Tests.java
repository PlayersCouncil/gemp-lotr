package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_214_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wake", "1_214");

					put("lemenya", "1_232");    // Ulaire Lemenya: a [wraith] Nazgul, strength 9, vitality 2; text on companions bearing [wraith] conditions only
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: a [raider] Nazgul, strength 12, fierce; the rest needs 3 burdens
					put("creeper", "8_75");     // Morgul Creeper: a [wraith] minion who is an Orc and no Nazgul, strength 8; nothing below 3 burdens

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound, not the Ring-bearer; his abilities are used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void IntheRingwraithsWakeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: In the Ringwraith's Wake
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Response
		 * Game Text: <b>Response:</b> If a Nazgûl wins a skirmish, the Free Peoples player chooses to either exert the Ring-bearer or add a burden.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("wake");

		assertEquals("In the Ringwraith's Wake", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhenANazgulWinsTheFreePeoplesPlayerChoosesAndMayExertTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wake = scn.GetShadowCard("wake");
		var lemenya = scn.GetShadowCard("lemenya");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveCardsToHand(wake);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lemenya);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Nobody has won anything yet.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(wake));

		// 9 against 8: the [wraith] Nazgul wins, and Aragorn, not the Ring-bearer, is the loser.
		scn.ShadowPass();
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.FreepsGetBurdens());
		assertTrue(scn.ShadowPlayAvailable(wake));
		scn.ShadowPlayCard(wake);

		// The Free Peoples player, not the Shadow player, chooses between the two.
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		assertTrue(scn.FreepsChoiceAvailable("Exert the Ring-bearer"));
		assertTrue(scn.FreepsChoiceAvailable("Add a burden"));
		scn.FreepsChoose("Exert the Ring-bearer");

		assertEquals(1, scn.GetWoundsOn(frodo));
		// Sam is Ring-bound but not the Ring-bearer.
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.FreepsGetBurdens());
		assertInDiscard(wake);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenANazgulOfAnotherCultureWinsTheFreePeoplesPlayerMayAddABurdenInstead() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wake = scn.GetShadowCard("wake");
		var khamul = scn.GetShadowCard("khamul");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(wake);

		scn.StartGame();
		scn.MoveMinionsToTable(khamul);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, khamul);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// 12 against 8, not doubled: the [raider] Nazgul wins.
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.FreepsGetBurdens());
		assertTrue(scn.ShadowPlayAvailable(wake));
		scn.ShadowPlayCard(wake);

		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		scn.FreepsChoose("Add a burden");

		assertEquals(1, scn.FreepsGetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInDiscard(wake);
		// Khamul is fierce: the fierce assignments come next.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithTheRingBearerExhaustedABurdenIsAddedWithNothingAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wake = scn.GetShadowCard("wake");
		var lemenya = scn.GetShadowCard("lemenya");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveCardsToHand(wake);

		scn.StartGame();
		// Vitality 4 with 3 wounds: Frodo is exhausted and cannot exert.  Sam beside him could.
		scn.AddWoundsToChar(frodo, 3);
		assertEquals(3, scn.GetWoundsOn(frodo));
		scn.MoveMinionsToTable(lemenya);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lemenya);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.FreepsGetBurdens());
		assertEquals(0, scn.GetWoundsOn(sam));
		scn.ShadowPlayCard(wake);

		// Exerting the Ring-bearer cannot be done, so there is nothing to choose: the burden is added.
		assertEquals(0, scn.FreepsGetMultipleChoiceCount());
		assertEquals(1, scn.FreepsGetBurdens());
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertInDiscard(wake);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenAWraithMinionWhoIsNoNazgulWinsNorWhenANazgulLoses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wake = scn.GetShadowCard("wake");
		var lemenya = scn.GetShadowCard("lemenya");
		var creeper = scn.GetShadowCard("creeper");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		// 8 + 5 = 13: Aragorn beats Lemenya (9) without overwhelming her, so that she lives through the loss.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 5));
		scn.MoveCardsToHand(wake);

		scn.StartGame();
		assertInHand(wake);
		scn.MoveMinionsToTable(lemenya, creeper);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, creeper}, new PhysicalCardImpl[]{aragorn, lemenya});

		// 8 against 6: Morgul Creeper, a [wraith] Orc, wins.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		// The Shadow player is asked nothing: where the event can be played, he is asked here.
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// 9 against 13: the Nazgul loses and lives.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertInPlay(lemenya);
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertInHand(wake);
		assertEquals(0, scn.FreepsGetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
