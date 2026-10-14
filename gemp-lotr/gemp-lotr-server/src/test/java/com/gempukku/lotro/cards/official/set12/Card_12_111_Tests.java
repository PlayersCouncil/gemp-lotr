package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_111_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("coif", "12_111");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, vitality 3; Valiant is his whole text
					// Guma, Plains Farmer: [rohan] Man ally, vitality 2; a fellowship ability used only on purpose
					put("guma", "4_277");
					// Merry, Master Holbytla: a [rohan] Hobbit companion, a Man only beside 3 [rohan] companions or Eowyn
					put("holbytla", "103_85");
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion; Knight is his whole text
					// Gimli, Dwarf of Erebor: a Free Peoples card whose fellowship ability adds (2) as its cost
					put("gimli", "1_12");
					put("runner", "1_178");     // Goblin Runner: when played, adds (2) unasked (F-0009)
					// Hollowing of Isengard: a Shadow condition, no character: "Shadow: Discard this condition to add (3)"
					put("hollowing", "3_54");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetForestToNonForestScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("coif", "12_111");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, Valiant is his whole text
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, Knight is his whole text
					put("guma", "4_277");       // Guma, Plains Farmer: [rohan] Man ally
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					// Bag End 1_319 (no forest) stays at site 1; Breeland Forest at site 2 is a forest ("Stealth
					// events may not be played"); Rivendell Valley 1_341 at site 3 is no forest.
					put("site2", "1_329");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetTwoForestsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("coif", "12_111");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, Valiant is his whole text
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, Knight is his whole text
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					// Hobbiton Woods (a forest; its fellowship ability is used only on purpose) and Breeland Forest
					put("site1", "2_116");
					put("site2", "1_329");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CoifStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Coif
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 0
		 * Type: Possession
		 * Subtype: Armor
		 * Vitality: +1
		 * Game Text: Bearer must be a [rohan] Man.<br>Each time a Shadow card adds any number of twilight tokens, you may exert bearer to prevent that.<br>Each time the fellowship moves from a forest site, exert a companion.
		*/

		var scn = GetScenario();

		var coif = scn.GetFreepsCard("coif");

		assertEquals("Coif", coif.getBlueprint().getTitle());
		assertNull(coif.getBlueprint().getSubtitle());
		assertFalse(coif.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, coif.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, coif.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, coif.getBlueprint().getCardType());
		assertTrue(coif.getBlueprint().getPossessionClasses().contains(PossessionClass.ARMOR));
		assertEquals(0, coif.getBlueprint().getTwilightCost());
		assertEquals(1, coif.getBlueprint().getVitality());
	}

	@Test
	public void PlaysOnARohanManCompanionOrAllyAndNotOnARohanHobbitOrAGondorMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var coif = scn.GetFreepsCard("coif");
		var guard = scn.GetFreepsCard("guard");
		var guma = scn.GetFreepsCard("guma");
		var holbytla = scn.GetFreepsCard("holbytla");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(guard, holbytla, knight);
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToHand(coif);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(coif));
		scn.FreepsPlayCard(coif);
		// Two [rohan] companions and no Eowyn: Merry is a Hobbit and no Man.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, guma));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(holbytla, knight));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(guma);
		assertEquals(guma, coif.getAttachedTo());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachTimeAShadowCardAddsTwilightBearerMayExertToPreventThat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var coif = scn.GetFreepsCard("coif");
		var guard = scn.GetFreepsCard("guard");
		var runner = scn.GetShadowCard("runner");
		var hollowing = scn.GetShadowCard("hollowing");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, coif);
		scn.MoveCardsToHand(runner);
		scn.MoveCardsToSupportArea(hollowing);

		scn.StartGame();
		scn.FreepsPass();

		// The move's twilight is added by no Shadow card: Coif was not offered, and the Shadow player is asked.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetWoundsOn(guard));
		int pool = scn.GetTwilight();

		// A Shadow minion's own text adds (2) as it is played.
		scn.ShadowPlayCard(runner);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(coif));
		scn.FreepsAcceptOptionalTrigger();
		// The Runner's cost is paid (1, and 2 for roaming at site 2); its (2) is not added.
		assertEquals(pool - 1 - 2, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A second time, by a Shadow card that is no character: offered again, and declined.
		scn.ShadowUseCardAction(hollowing);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(coif));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(pool - 1 - 2 + 3, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(guard));
		assertInZone(Zone.DISCARD, hollowing);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnAllyBearingCoifExertsToPreventTheTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var coif = scn.GetFreepsCard("coif");
		var guard = scn.GetFreepsCard("guard");
		var guma = scn.GetFreepsCard("guma");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(guard);
		scn.MoveAlliesToTable(guma);
		scn.AttachCardsTo(guma, coif);
		scn.MoveCardsToHand(runner);

		scn.StartGame();
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetWoundsOn(guma));
		assertEquals(0, scn.GetWoundsOn(guard));
		int pool = scn.GetTwilight();

		scn.ShadowPlayCard(runner);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(coif));
		scn.FreepsAcceptOptionalTrigger();
		// Guma, the bearer, exerts; the companion beside him does not.
		assertEquals(1, scn.GetWoundsOn(guma));
		assertEquals(0, scn.GetWoundsOn(guard));
		// 1 for the Runner and 2 for roaming paid; its (2) not added.
		assertEquals(pool - 1 - 2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWhileBearerIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var coif = scn.GetFreepsCard("coif");
		var guard = scn.GetFreepsCard("guard");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, coif);
		scn.MoveCardsToHand(runner);

		scn.StartGame();
		// Vitality 3, +1 for the Coif: 3 wounds leave him exhausted.
		scn.AddWoundsToChar(guard, 3);
		scn.FreepsPass();

		assertEquals(1, scn.GetVitality(guard));
		assertTrue(scn.AwaitingShadowPhaseActions());
		int pool = scn.GetTwilight();

		scn.ShadowPlayCard(runner);
		// Not offered: the Runner's cost is paid (1, and 2 for roaming) and its (2) added.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(coif));
		assertEquals(pool - 1 - 2 + 2, scn.GetTwilight());
		assertEquals(3, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWhenAFreePeoplesCardAddsTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var coif = scn.GetFreepsCard("coif");
		var guard = scn.GetFreepsCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(guard, gimli);
		scn.AttachCardsTo(guard, coif);
		scn.MoveCardsToHand(knight);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(guard));
		assertTrue(scn.FreepsActionAvailable(gimli));

		scn.FreepsUseCardAction(gimli);
		// Gimli's (2), added by a Free Peoples card, is not offered for prevention.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(coif));
		assertEquals(2, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void MovingFromAForestSiteExertsACompanionAndMovingToOneDoesNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetForestToNonForestScenario();

		var coif = scn.GetFreepsCard("coif");
		var guard = scn.GetFreepsCard("guard");
		var knight = scn.GetFreepsCard("knight");
		var guma = scn.GetFreepsCard("guma");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(guard, knight);
		scn.MoveAlliesToTable(guma);
		scn.AttachCardsTo(guard, coif);

		scn.StartGame();

		// From Bag End to Breeland Forest: a move TO a forest. Nobody exerts and the Shadow player is asked.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(guma));

		// From Breeland Forest to Rivendell Valley, in the regroup phase.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		// Any companion: the Ring-bearer, the bearer and a [gondor] Man; not the ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, guard, knight));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guma));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(knight);
		assertEquals(1, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(guma));
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachMoveFromAForestSiteExertsACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTwoForestsScenario();

		var coif = scn.GetFreepsCard("coif");
		var guard = scn.GetFreepsCard("guard");
		var knight = scn.GetFreepsCard("knight");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(guard, knight);
		scn.AttachCardsTo(guard, coif);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(knight));

		// From Hobbiton Woods to Breeland Forest.
		scn.FreepsPass();
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, guard, knight));
		scn.FreepsChooseCard(guard);
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// From Breeland Forest to Rivendell Valley: again.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, guard, knight));
		scn.FreepsChooseCard(knight);
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
