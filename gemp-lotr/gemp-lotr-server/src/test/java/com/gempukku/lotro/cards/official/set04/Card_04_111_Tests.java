package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_111_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("brother", "4_111");
					// Faramir, Son of Denethor: Ring-bound Man; an opponent may not play skirmish events or
					// abilities in his skirmishes
					put("faramir", "4_117");
					// Damrod, Ranger of Ithilien: a [gondor] Ring-bound Man who is no Faramir; a Skirmish ability
					put("damrod", "4_114");
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound, and a Hobbit
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man, and unbound
					// [gondor] cards of three kinds for the hand, and one of another culture.
					put("mablung", "4_127");    // Mablung, Soldier of Gondor: a [gondor] companion
					put("bow", "4_131");        // Ranger's Bow: a [gondor] possession
					put("defend", "4_115");     // Defend It and Hope: a [gondor] event
					put("gandalf", "1_364");    // Gandalf, The Grey Wizard: a [gandalf] companion
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13; no text
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, strength 8; Damage +1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BoromirMyBrotherStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Boromir, My Brother
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: 
		 * Vitality: +1
		 * Game Text: Bearer must be Faramir.<br><b>Skirmish:</b> Discard a [gondor] card from hand to make a Ring-bound Man strength +1.
		*/

		var scn = GetScenario();

		var brother = scn.GetFreepsCard("brother");

		assertEquals("Boromir, My Brother", brother.getBlueprint().getTitle());
		assertNull(brother.getBlueprint().getSubtitle());
		assertTrue(brother.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, brother.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, brother.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, brother.getBlueprint().getCardType());
		assertEquals(0, brother.getBlueprint().getTwilightCost());
		assertEquals(1, brother.getBlueprint().getVitality());
	}

	@Test
	public void CannotBePlayedUntilFaramirIsInPlayAndThenGoesOnHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brother = scn.GetFreepsCard("brother");
		var faramir = scn.GetFreepsCard("faramir");
		var damrod = scn.GetFreepsCard("damrod");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(damrod, aragorn);
		scn.MoveCardsToHand(brother, faramir);

		scn.StartGame();
		// A [gondor] Ring-bound Man and a [gondor] Man, neither of them Faramir.
		assertInHand(brother, faramir);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(brother));

		scn.FreepsPlayCard(faramir);
		assertInZone(Zone.FREE_CHARACTERS, faramir);
		assertTrue(scn.FreepsPlayAvailable(brother));

		// Faramir is the one bearer it can have: it goes on him with nothing asked.
		scn.FreepsPlayCard(brother);
		assertAttachedTo(brother, faramir);
		assertNotAttachedTo(brother, damrod);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilityDiscardsAGondorCardToMakeARingBoundManStrongerForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brother = scn.GetFreepsCard("brother");
		var faramir = scn.GetFreepsCard("faramir");
		var damrod = scn.GetFreepsCard("damrod");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var mablung = scn.GetFreepsCard("mablung");
		var bow = scn.GetFreepsCard("bow");
		var defend = scn.GetFreepsCard("defend");
		var gandalf = scn.GetFreepsCard("gandalf");
		var frodo = scn.GetRingBearer();
		var troop = scn.GetShadowCard("troop");
		var sentry = scn.GetShadowCard("sentry");
		scn.MoveCompanionsToTable(faramir, damrod, sam, aragorn);
		scn.AttachCardsTo(faramir, brother);
		scn.MoveCardsToHand(mablung, bow, defend, gandalf);
		assertAttachedTo(brother, faramir);

		scn.StartGame();
		assertInHand(mablung, bow, defend, gandalf);
		scn.MoveMinionsToTable(troop, sentry);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{faramir, troop}, new PhysicalCardImpl[]{aragorn, sentry});
		scn.FreepsResolveSkirmish(faramir);
		assertEquals(6, scn.GetStrength(damrod));
		assertEquals(7, scn.GetStrength(faramir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(brother));

		scn.FreepsUseCardAction(brother);
		// A [gondor] card of any kind, and not a card of another culture.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(mablung, bow, defend));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gandalf));
		scn.FreepsChooseCard(defend);
		assertInDiscard(defend);
		assertInHand(mablung, bow, gandalf);

		// A Ring-bound Man in this skirmish or out of it; not a Ring-bound Hobbit, nor an unbound Man.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(faramir, damrod));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sam, frodo, aragorn));
		scn.FreepsChooseCard(damrod);
		assertEquals(7, scn.GetStrength(damrod));
		assertEquals(7, scn.GetStrength(faramir));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The skirmish ends; in the next one, a skirmish phase of its own, the bonus is gone.
		scn.PassCurrentPhaseActions();
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(6, scn.GetStrength(damrod));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWithNoGondorCardInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brother = scn.GetFreepsCard("brother");
		var faramir = scn.GetFreepsCard("faramir");
		var defend = scn.GetFreepsCard("defend");
		var gandalf = scn.GetFreepsCard("gandalf");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(faramir);
		scn.AttachCardsTo(faramir, brother);
		scn.MoveCardsToHand(gandalf);
		assertAttachedTo(brother, faramir);

		scn.StartGame();
		assertInHand(gandalf);
		scn.MoveMinionsToTable(troop);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(faramir, troop);
		scn.FreepsResolveSkirmish(faramir);
		// A [gandalf] card is all there is to discard.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(brother));

		// A [gondor] card in hand, and it is offered.
		scn.MoveCardsToHand(defend);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(brother));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
