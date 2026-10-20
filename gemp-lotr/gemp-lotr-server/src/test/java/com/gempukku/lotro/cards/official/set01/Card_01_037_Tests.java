package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_037_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("defiance", "1_37");
					put("defiance2", "1_37");
					put("defiance3", "1_37");
					// Legolas, Greenleaf: Elf companion, strength 6, an archer, whose archery wound goes to the one minion on the table then
					put("legolas", "1_50");
					put("haldir", "1_48");      // Haldir, Elf of the Golden Wood: Elf companion, strength 5; +2 only at sites 6 to 8
					// Silinde, Elf of Mirkwood: an Elf ally (home site 3), in the support area here
					put("silinde", "1_60");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Elf, strength 8
					// Morgul Hound: a [wraith] Orc, no Nazgûl, strength 5, vitality 2; +4 and fierce only at 2 wounds
					// on the Ring-bearer
					put("hound", "7_192");
					// Úlairë Lemenya, Lieutenant of Morgul: a [wraith] Nazgûl, strength 9, vitality 2; his text reaches
					// only characters bearing a [wraith] condition
					put("lemenya", "1_232");
					// Ulaire Attea, Khamul of the East: a [raider] Nazgûl, strength 12, fierce; his text needs 3 burdens
					put("khamul", "103_61");
					// (Arwen 1_30 is left out: she is strength +3 herself while skirmishing a Nazgûl.)
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DefianceStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Defiance
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Make an Elf strength +2 (or +4 if skirmishing a Nazgûl).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("defiance");

		assertEquals("Defiance", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesAnElfStrengthPlus2UntilTheEndOfTheSkirmishWhenHisMinionIsNoNazgul() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var defiance = scn.GetFreepsCard("defiance");
		var legolas = scn.GetFreepsCard("legolas");
		var haldir = scn.GetFreepsCard("haldir");
		var silinde = scn.GetFreepsCard("silinde");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		var hound = scn.GetShadowCard("hound");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCompanionsToTable(legolas, haldir, aragorn);
		scn.MoveAlliesToTable(silinde);
		scn.MoveCardsToHand(defiance);

		scn.StartGame();

		scn.MoveMinionsToTable(lemenya);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		// The Hound goes down after the archery phase, so that Legolas's archery wound has one minion to go to.
		scn.MoveMinionsToTable(hound);
		scn.FreepsRefreshDecision();
		assertInHand(defiance);

		// Not in the assignment phase, the phase before the skirmishes.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(defiance));

		scn.PassAssignmentActions();
		// Legolas fights a [wraith] minion that is no Nazgûl; the Nazgûl is in Aragorn's skirmish.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{legolas, hound}, new PhysicalCardImpl[]{aragorn, lemenya});
		scn.FreepsResolveSkirmish(legolas);

		assertEquals(6, scn.GetStrength(legolas));
		assertEquals(5, scn.GetStrength(haldir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(defiance));

		scn.FreepsPlayCard(defiance);
		// Each Elf is offered: in this skirmish, in none, and an ally; no other companion is.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, haldir, silinde));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo));
		scn.FreepsChooseCard(legolas);

		// 6 + 2
		assertEquals(8, scn.GetStrength(legolas));
		assertEquals(5, scn.GetStrength(haldir));
		assertInDiscard(defiance);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Legolas (8) beats the Hound (5); in Aragorn's skirmish, which follows, he is back to 6.
		assertEquals(0, scn.GetWoundsOn(hound));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(hound));
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, lemenya));
		assertEquals(6, scn.GetStrength(legolas));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void MakesAnElfSkirmishingANazgulStrengthPlus4AndOneWaitingForHisSkirmishPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var defiance = scn.GetFreepsCard("defiance");
		var defiance2 = scn.GetFreepsCard("defiance2");
		var defiance3 = scn.GetFreepsCard("defiance3");
		var legolas = scn.GetFreepsCard("legolas");
		var haldir = scn.GetFreepsCard("haldir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var hound = scn.GetShadowCard("hound");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		scn.MoveCompanionsToTable(legolas, haldir, aragorn);
		scn.MoveCardsToHand(defiance, defiance2, defiance3);

		scn.StartGame();
		assertInHand(defiance, defiance2, defiance3);

		scn.MoveMinionsToTable(hound);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		// The Nazgûl, of two cultures, go down after the archery phase, so that Legolas's archery wound has one
		// minion to go to.
		scn.MoveMinionsToTable(lemenya, khamul);
		scn.PassAssignmentActions();
		// Legolas's skirmish is fought first, Haldir's after it.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{legolas, lemenya}, new PhysicalCardImpl[]{haldir, khamul},
				new PhysicalCardImpl[]{aragorn, hound});
		scn.FreepsResolveSkirmish(legolas);

		assertEquals(6, scn.GetStrength(legolas));
		assertEquals(5, scn.GetStrength(haldir));
		assertInHand(defiance, defiance2, defiance3);

		scn.FreepsPlayCard(defiance);
		scn.FreepsChooseCard(legolas);
		// 6 + 4: he is skirmishing a Nazgûl.
		assertEquals(10, scn.GetStrength(legolas));

		scn.ShadowPass();
		scn.FreepsPlayCard(defiance2);
		scn.FreepsChooseCard(haldir);
		// 5 + 2: Haldir is assigned to a Nazgûl, but his skirmish has not begun.
		assertEquals(7, scn.GetStrength(haldir));
		assertEquals(10, scn.GetStrength(legolas));
		assertInDiscard(defiance, defiance2);

		// Legolas (10) beats Lemenya (9); in Haldir's own skirmish both are as printed again.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(haldir);
		assertTrue(scn.IsCharSkirmishingAgainst(haldir, khamul));
		assertEquals(5, scn.GetStrength(haldir));
		assertEquals(6, scn.GetStrength(legolas));

		scn.FreepsPlayCard(defiance3);
		scn.FreepsChooseCard(haldir);
		// 5 + 4: the [raider] Nazgûl counts as the [wraith] one did.
		assertEquals(9, scn.GetStrength(haldir));
		assertInDiscard(defiance3);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void MayBePlayedWithNoElfInPlayAndThenDoesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var defiance = scn.GetFreepsCard("defiance");
		var aragorn = scn.GetFreepsCard("aragorn");
		var hound = scn.GetShadowCard("hound");
		// No Elf is in play: the only companion is Aragorn, who is none.
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defiance);

		scn.StartGame();
		assertInHand(defiance);
		scn.MoveMinionsToTable(hound);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, hound);

		assertEquals(8, scn.GetStrength(aragorn));
		// Nothing in the text is a cost or a requirement, so the event can be played for nothing.
		assertTrue(scn.FreepsPlayAvailable(defiance));

		scn.FreepsPlayCard(defiance);
		assertInDiscard(defiance);
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
