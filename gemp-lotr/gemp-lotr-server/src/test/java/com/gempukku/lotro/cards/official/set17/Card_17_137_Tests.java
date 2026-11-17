package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_137_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fear", "17_137");
					put("uruk", "11_204");      // Tyrannical Uruk: [uruk-hai] Uruk-hai minion; damage +1 and toil only
					put("saruman", "17_116");   // Saruman, Master of the White Hand: an [uruk-hai] minion who is no Uruk-hai; companions resistance -1, a skirmish ability
					put("guard", "10_81");      // Cirith Ungol Guard: an Uruk-hai who is [sauron]; damage +1 only
					put("runner", "1_178");     // Goblin Runner: a [moria] Orc, a minion of neither the culture nor the race
					put("isword", "11_192");    // Isengard Sword: an [uruk-hai] possession, borne by an [uruk-hai] minion
					put("goaded", "12_67");     // Goaded to War: a Shadow support-area condition; adds (1) as the fellowship moves to plains
					put("wielding", "11_105");  // Wielding the Ring: a Shadow condition borne by a companion; resistance -2 only

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion to bear cards
					put("saga", "1_114");       // The Saga of Elendil: a Free Peoples condition borne by a companion; a skirmish ability
					put("rsword", "1_112");     // Ranger's Sword: a Free Peoples possession borne by Aragorn; damage +1 only
					put("mallorn", "1_54");     // Mallorn-trees: a Free Peoples support-area condition; a fellowship ability
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void YouDoNotKnowFearStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: You Do Not Know Fear
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Discard a condition for each [uruk-hai] minion you can spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fear");

		assertEquals("You Do Not Know Fear", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void DiscardsAConditionForEachUrukHaiCultureMinionSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var saga = scn.GetFreepsCard("saga");
		var rsword = scn.GetFreepsCard("rsword");
		var mallorn = scn.GetFreepsCard("mallorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, saga, rsword);
		scn.MoveCardsToSupportArea(mallorn);

		var fear = scn.GetShadowCard("fear");
		var uruk = scn.GetShadowCard("uruk");
		var saruman = scn.GetShadowCard("saruman");
		var runner = scn.GetShadowCard("runner");
		var isword = scn.GetShadowCard("isword");
		var goaded = scn.GetShadowCard("goaded");
		var wielding = scn.GetShadowCard("wielding");
		scn.MoveCardsToHand(fear);

		scn.StartGame();
		scn.MoveMinionsToTable(uruk, saruman, runner);
		scn.AttachCardsTo(uruk, isword);
		scn.MoveCardsToSupportArea(goaded);
		scn.AttachCardsTo(aragorn, wielding);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Two [uruk-hai] minions, the Uruk and Saruman.  Counting Uruk-hai would make it 1, every minion 3, and
		// every [uruk-hai] card 3 (the Isengard Sword).
		assertInZone(Zone.SUPPORT, mallorn, goaded);
		assertAttachedTo(saga, aragorn);
		assertAttachedTo(wielding, aragorn);
		assertTrue(scn.ShadowPlayAvailable(fear));

		scn.ShadowPlayCard(fear);
		assertTrue(scn.ShadowHasCardChoiceAvailable(mallorn, saga, goaded, wielding));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(rsword, isword));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCards(saga, goaded);
		assertInDiscard(saga, goaded, fear);
		assertInZone(Zone.SUPPORT, mallorn);
		assertAttachedTo(wielding, aragorn);
		assertAttachedTo(rsword, aragorn);
		assertAttachedTo(isword, uruk);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AnUrukHaiOfAnotherCultureIsNotCounted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mallorn = scn.GetFreepsCard("mallorn");
		scn.MoveCardsToSupportArea(mallorn);

		var fear = scn.GetShadowCard("fear");
		var uruk = scn.GetShadowCard("uruk");
		var guard = scn.GetShadowCard("guard");
		var goaded = scn.GetShadowCard("goaded");
		scn.MoveCardsToHand(fear);

		scn.StartGame();
		scn.MoveMinionsToTable(uruk, guard);
		scn.MoveCardsToSupportArea(goaded);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// One [uruk-hai] minion: the Guard is an Uruk-hai of [sauron].  Counting Uruk-hai or minions would make it 2.
		assertInZone(Zone.SUPPORT, mallorn, goaded);

		scn.ShadowPlayCard(fear);
		assertTrue(scn.ShadowHasCardChoiceAvailable(mallorn, goaded));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCards(mallorn);
		assertInDiscard(mallorn, fear);
		assertInZone(Zone.SUPPORT, goaded);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mallorn = scn.GetFreepsCard("mallorn");
		scn.MoveCardsToSupportArea(mallorn);

		var fear = scn.GetShadowCard("fear");
		var uruk = scn.GetShadowCard("uruk");
		scn.MoveCardsToHand(fear);

		scn.StartGame();
		scn.MoveMinionsToTable(uruk);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		// A condition to discard and an [uruk-hai] minion to spot, with the twilight to pay for it.
		assertInZone(Zone.SUPPORT, mallorn);
		assertTrue(scn.GetTwilight() >= 2);
		assertFalse(scn.ShadowPlayAvailable(fear));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}
}
