package com.gempukku.lotro.cards.unofficial.pc.errata.set02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_076_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("helpless", "52_76");
					put("helpless2", "52_76");

					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound; Fellowship: exert to remove a burden; Response: if Frodo dies, become the Ring-bearer
					put("merry", "1_303");      // Merry, From O'er the Brandywine: a Hobbit companion who is not Ring-bound
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is neither Hobbit nor Ring-bound

					put("rider", "12_161");     // Black Rider: [ringwraith] Nazgul, vitality 3, no special ability
					put("attea", "103_61");     // Ulaire Attea, Khamul of the East: [raider] Nazgul
					put("faces", "16_4");       // Dead Faces: [ringwraith] Wraith and not a Nazgul
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, no text; overwhelms Frodo
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HelplessStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Helpless
		 * Unique: False
		 * Side: Shadow
		 * Culture: Ringwraith
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Bearer's special abilities cannot be used.
		 * Maneuver: If this is in your support area, exert a Nazgul to transfer this to a Ring-bound companion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("helpless");

		assertEquals("Helpless", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void BearerCannotUseHisFellowshipAbilityOrHisResponse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var helpless = scn.GetShadowCard("helpless");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(sam);
		scn.AttachCardsTo(sam, helpless);

		scn.StartGame();
		scn.AddBurdens(1);
		scn.FreepsRefreshDecision();

		// A burden to remove and Sam able to exert: only Helpless stands in the way.
		assertEquals(1, scn.GetBurdens());
		assertFalse(scn.FreepsActionAvailable(sam));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, troop);
		scn.FreepsResolveSkirmish(frodo);
		scn.PassSkirmishActions();

		// 13 against 3: Frodo is overwhelmed, Sam cannot take the Ring, and the game is over.
		assertInZone(Zone.DEAD, frodo);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(sam));
		assertTrue(scn.GameIsFinished());
	}

	@Test
	public void ACompanionWhoDoesNotBearItMayUseHisAbilities() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var helpless = scn.GetShadowCard("helpless");
		var helpless2 = scn.GetShadowCard("helpless2");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(sam);
		// One on another companion, one in the support area, bearing nothing.
		scn.AttachCardsTo(frodo, helpless);
		scn.MoveCardsToSupportArea(helpless2);

		scn.StartGame();
		scn.AddBurdens(1);

		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.FreepsActionAvailable(sam));
		scn.FreepsUseCardAction(sam);
		assertEquals(0, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(sam));

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, troop);
		scn.FreepsResolveSkirmish(frodo);
		scn.PassSkirmishActions();

		assertInZone(Zone.DEAD, frodo);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sam));
		assertFalse(scn.GameIsFinished());
	}

	@Test
	public void ManeuverExertsANazgulToTransferThisCopyFromTheSupportAreaToARingBoundCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var helpless = scn.GetShadowCard("helpless");
		var helpless2 = scn.GetShadowCard("helpless2");
		var rider = scn.GetShadowCard("rider");
		var attea = scn.GetShadowCard("attea");
		var faces = scn.GetShadowCard("faces");
		scn.MoveCompanionsToTable(sam, merry, aragorn);
		scn.MoveCardsToSupportArea(helpless, helpless2);

		scn.StartGame();
		scn.MoveMinionsToTable(rider, attea, faces);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(helpless));
		assertEquals(0, scn.GetWoundsOn(rider));
		int twilight = scn.GetTwilight();
		scn.ShadowUseCardAction(helpless);

		// Either Nazgul, of whatever culture; not the Wraith who is no Nazgul.
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(rider));
		assertTrue(scn.ShadowHasCardChoiceAvailable(attea));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(faces));
		scn.ShadowChooseCard(rider);
		assertEquals(1, scn.GetWoundsOn(rider));
		assertEquals(0, scn.GetWoundsOn(attea));
		assertEquals(0, scn.GetWoundsOn(faces));

		// The Ring-bearer and Sam; not Merry, a Hobbit who is not Ring-bound, nor Aragorn.
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo));
		assertTrue(scn.ShadowHasCardChoiceAvailable(sam));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(merry));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn));
		scn.ShadowChooseCard(sam);

		assertInZone(Zone.ATTACHED, helpless);
		assertEquals(sam, helpless.getAttachedTo());
		assertInZone(Zone.SUPPORT, helpless2);
		// A transfer by an effect costs no twilight.
		assertEquals(twilight, scn.GetTwilight());

		// Once borne, this copy offers nothing; the copy still in the support area does.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		scn.FreepsPass();
		assertFalse(scn.ShadowActionAvailable(helpless));
		assertTrue(scn.ShadowActionAvailable(helpless2));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ManeuverAbilityIsNotOfferedWhenTheOnlyNazgulIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var helpless = scn.GetShadowCard("helpless");
		var rider = scn.GetShadowCard("rider");
		var faces = scn.GetShadowCard("faces");
		scn.MoveCardsToSupportArea(helpless);

		scn.StartGame();
		scn.MoveMinionsToTable(rider, faces);
		// Vitality 3 with 2 wounds: exhausted. Dead Faces could exert, but is no Nazgul.
		scn.AddWoundsToChar(rider, 2);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertFalse(scn.ShadowActionAvailable(helpless));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
