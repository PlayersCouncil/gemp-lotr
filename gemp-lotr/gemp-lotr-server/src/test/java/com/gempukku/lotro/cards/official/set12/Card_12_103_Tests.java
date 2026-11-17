package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_103_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("storming", "12_103");
					// Denizen of Khazad-dum: an [orc] Orc minion of vitality 2; strength +2 only at an underground site.
					put("denizen", "11_115");
					// Half-troll of Far Harad: an [orc] minion who is no Orc; its start-of-assignment trigger is never reached.
					put("halftroll", "14_12");
					// Goblin Patrol Troop: a [moria] Orc with no text.
					put("troop", "1_177");
					// The Beckoning Shadow: an [orc] condition (support area); cheated in, its text needs 5 burdens.
					put("beckoning", "12_83");
					// Weary: an [isengard] condition the Shadow player puts on a companion; it acts only on an
					// assignment to an [isengard] tracker.
					put("weary", "4_212");

					// Gondor Bowmen: a [gondor] condition in the Free Peoples support area (its archery ability is never used).
					put("bowmen", "3_41");
					// There and Back Again: a [shire] condition borne by a Hobbit companion (Frodo here).
					put("back", "1_317");
					// Red Book of Westmarch: a [shire] possession in the support area; it draws only for a tale played
					// while Bilbo is spotted.
					put("redbook", "2_113");
					// Aragorn, Ranger of the North: a companion to bear Weary.  His maneuver ability is never used.
					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StormingtheRampartsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Storming the Ramparts
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Toil 2</b>. <helper>(For each [orc] character you exert when playing this, its twilight cost is -2.)</helper><br>Spot an [orc] minion to make the Free Peoples player discard one of his or her conditions from play.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("storming");

		assertEquals("Storming the Ramparts", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TOIL));
		assertEquals(2, scn.GetKeywordCount(card, Keyword.TOIL));
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ToilTakesTwoOffItsCostForEachOrcCharacterExertedAsItIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var storming = scn.GetShadowCard("storming");
		var denizen = scn.GetShadowCard("denizen");
		var halftroll = scn.GetShadowCard("halftroll");
		var troop = scn.GetShadowCard("troop");
		var bowmen = scn.GetFreepsCard("bowmen");
		scn.MoveCardsToSupportArea(bowmen);

		scn.StartGame();
		scn.SetShadowHand(storming);
		scn.MoveMinionsToTable(denizen, halftroll, troop);
		// Vitality 2 with one wound: the Half-troll cannot exert, and does not count for toil.
		scn.AddWoundsToChar(halftroll, 1);
		assertInHand(storming);
		assertInZone(Zone.SUPPORT, bowmen);
		assertEquals(0, scn.GetWoundsOn(denizen));
		assertEquals(1, scn.GetWoundsOn(halftroll));
		assertEquals(0, scn.GetWoundsOn(troop));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// 3 - 2 is the least it can cost, with the one [orc] character able to exert; the [moria] Troop does not
		// count.  Toil 1 would want 2, and toil 3 (or the Troop counted) would let 0 do.
		scn.SetTwilight(0);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(storming));
		scn.SetTwilight(1);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(storming));

		scn.ShadowPlayCard(storming);
		// The one exertion owed is the only one there is: it is made unasked.
		assertEquals(1, scn.GetWoundsOn(denizen));
		assertEquals(1, scn.GetWoundsOn(halftroll));
		assertEquals(0, scn.GetWoundsOn(troop));
		// 1 - (3 - 2)
		assertEquals(0, scn.GetTwilight());

		// The Free Peoples player's one condition goes.
		assertInDiscard(bowmen);
		assertInDiscard(storming);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void MakesTheFreePeoplesPlayerDiscardOneOfHisConditionsBorneOrInTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var storming = scn.GetShadowCard("storming");
		var halftroll = scn.GetShadowCard("halftroll");
		var weary = scn.GetShadowCard("weary");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var bowmen = scn.GetFreepsCard("bowmen");
		var back = scn.GetFreepsCard("back");
		var redbook = scn.GetFreepsCard("redbook");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(bowmen, redbook);
		scn.AttachCardsTo(frodo, back);
		scn.AttachCardsTo(aragorn, weary);

		scn.StartGame();
		scn.SetShadowHand(storming);
		// The only [orc] minion is no Orc: "an [orc] minion" goes by culture.
		scn.MoveMinionsToTable(halftroll);
		assertInZone(Zone.SUPPORT, bowmen);
		assertInZone(Zone.SUPPORT, redbook);
		assertInZone(Zone.ATTACHED, back);
		assertInZone(Zone.ATTACHED, weary);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(storming));
		scn.ShadowPlayCard(storming);
		// The pool pays in full: toil is still asked, from 0, and declined.
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowDeclineChoosing();
		assertEquals(0, scn.GetWoundsOn(halftroll));

		// The Free Peoples player chooses one of his conditions: the one in his support area and the one Frodo
		// bears.  The Shadow player's condition on Aragorn, and his own possession, are not his to choose.
		assertTrue(scn.FreepsHasCardChoiceAvailable(bowmen, back));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(weary, redbook));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(back);

		assertInDiscard(back);
		assertInZone(Zone.SUPPORT, bowmen);
		assertInZone(Zone.SUPPORT, redbook);
		assertInZone(Zone.ATTACHED, weary);
		assertInDiscard(storming);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutAnOrcMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var storming = scn.GetShadowCard("storming");
		var troop = scn.GetShadowCard("troop");
		var beckoning = scn.GetShadowCard("beckoning");
		var denizen = scn.GetShadowCard("denizen");
		var bowmen = scn.GetFreepsCard("bowmen");
		scn.MoveCardsToSupportArea(bowmen);

		scn.StartGame();
		scn.SetShadowHand(storming);
		// A [moria] Orc, and an [orc] card that is no minion.
		scn.MoveMinionsToTable(troop);
		scn.MoveCardsToSupportArea(beckoning);
		assertInZone(Zone.SUPPORT, beckoning);
		assertInZone(Zone.SUPPORT, bowmen);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(storming));

		// An [orc] minion comes: it can be played.
		scn.MoveMinionsToTable(denizen);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(storming));
	}

	@Test
	public void WithNoFreePeoplesConditionInPlayItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var storming = scn.GetShadowCard("storming");
		var denizen = scn.GetShadowCard("denizen");
		var weary = scn.GetShadowCard("weary");
		var aragorn = scn.GetFreepsCard("aragorn");
		var redbook = scn.GetFreepsCard("redbook");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(redbook);
		scn.AttachCardsTo(aragorn, weary);

		scn.StartGame();
		scn.SetShadowHand(storming);
		scn.MoveMinionsToTable(denizen);
		assertInZone(Zone.SUPPORT, redbook);
		assertInZone(Zone.ATTACHED, weary);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(storming));
		scn.ShadowPlayCard(storming);
		scn.ShadowDeclineChoosing();

		// Nothing for the Free Peoples player to discard: he is asked nothing, and both cards stay.
		assertInZone(Zone.SUPPORT, redbook);
		assertInZone(Zone.ATTACHED, weary);
		assertInDiscard(storming);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
