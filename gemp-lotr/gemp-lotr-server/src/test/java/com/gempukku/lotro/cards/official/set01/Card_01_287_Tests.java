package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_287_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("resilience", "1_287");
					// The Gaffer, Sam's Father: Hobbit ally, vitality 2; "Fellowship: Exert The Gaffer to heal Frodo or Sam."
					put("gaffer", "1_291");
					// Sam, Son of Hamfast: a Ring-bound companion who is not the Ring-bearer; his fellowship ability (exert
					// to remove a burden) is never used here
					put("sam", "1_311");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetHealScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("resilience", "1_287");
					// The Gaffer, Sam's Father: "Fellowship: Exert The Gaffer to heal Frodo or Sam."
					put("gaffer", "1_291");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Minas Tirith Third Circle as site 6: "If the fellowship moves from this site during the regroup phase, heal each
	// Hobbit companion", one heal that names the Ring-bearer and Sam together.
	protected VirtualTableScenario GetTwoHobbitsHealScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("resilience", "1_287");
					// Sam, Son of Hamfast: a Hobbit companion who is not the Ring-bearer; his fellowship ability (exert to
					// remove a burden) is never used here
					put("sam", "1_311");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site6", "7_352");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ExtraordinaryResilienceStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Extraordinary Resilience
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Response
		 * Game Text: <b>Response:</b> If the Ring-bearer is about to heal, remove a burden instead.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("resilience");

		assertEquals("Extraordinary Resilience", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ResponseRemovesABurdenInsteadOfTheRingBearerHealing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetHealScenario();

		var resilience = scn.GetFreepsCard("resilience");
		var gaffer = scn.GetFreepsCard("gaffer");
		var frodo = scn.GetRingBearer();
		scn.MoveAlliesToTable(gaffer);
		scn.MoveCardsToHand(resilience);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 1);
		// Two burdens, so that "a burden" is told from every burden.
		scn.AddBurdens(2);

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(gaffer));
		assertTrue(scn.FreepsActionAvailable(gaffer));

		// Frodo is the only Frodo or Sam, so the Gaffer's heal is his without a choice.
		scn.FreepsUseCardAction(gaffer);
		assertEquals(1, scn.GetWoundsOn(gaffer));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsPlayAvailable(resilience));

		scn.FreepsPlayCard(resilience);
		// The wound stays, a burden goes, and what the Gaffer paid stays paid.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(gaffer));
		assertInZone(Zone.DISCARD, resilience);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedWhenARingBoundCompanionWhoIsNotTheRingBearerIsAboutToHeal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var resilience = scn.GetFreepsCard("resilience");
		var gaffer = scn.GetFreepsCard("gaffer");
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(sam);
		scn.MoveAlliesToTable(gaffer);
		scn.MoveCardsToHand(resilience);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(sam, 1);
		scn.AddBurdens(2);

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetBurdens());
		assertInHand(resilience);
		assertTrue(scn.FreepsActionAvailable(gaffer));

		scn.FreepsUseCardAction(gaffer);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam));
		scn.FreepsChooseCard(sam);

		// No response is asked for Sam's heal: he is healed, and the game goes on to the next fellowship action.
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetBurdens());
		assertInHand(resilience);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OnlyTheRingBearersHealIsReplacedWhenAnotherCompanionIsHealedWithHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTwoHobbitsHealScenario();

		var resilience = scn.GetFreepsCard("resilience");
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(resilience);

		scn.StartGame();
		scn.SkipToSite(5);
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(sam, 1);
		scn.AddBurdens(2);

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetBurdens());
		assertInHand(resilience);

		// The move to the Third Circle heals nobody; the regroup move from it heals every Hobbit companion at once.
		scn.FreepsPass();
		assertEquals(6, scn.GetCurrentSiteNumber());
		scn.SkipToMovementDecision();
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));
		scn.FreepsChooseToMove();

		// One heal names the Ring-bearer and Sam together, and the response is offered for it.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertTrue(scn.FreepsPlayAvailable(resilience));
		scn.FreepsPlayCard(resilience);

		// Only the Ring-bearer's heal is replaced: his wound stays and a burden goes, and Sam is healed as he would
		// have been. No choice among the companions is asked.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.DISCARD, resilience);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoBurdenToRemoveTheRingBearerIsStillHealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var resilience = scn.GetFreepsCard("resilience");
		var gaffer = scn.GetFreepsCard("gaffer");
		var frodo = scn.GetRingBearer();
		scn.MoveAlliesToTable(gaffer);
		scn.MoveCardsToHand(resilience);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 1);

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsActionAvailable(gaffer));

		// Frodo is the only wounded Frodo or Sam, so the Gaffer's heal is his without a choice.
		scn.FreepsUseCardAction(gaffer);
		// An event is offered whenever its requirements and costs are met, whatever its effect will do (Q-0034).
		assertTrue(scn.FreepsPlayAvailable(resilience));

		scn.FreepsPlayCard(resilience);
		// No burden can be removed, so the heal it would have replaced happens (CR 4.2 "instead": "If the second
		// effect cannot happen for any reason, then the original effect occurs").
		assertInDiscard(resilience);
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
