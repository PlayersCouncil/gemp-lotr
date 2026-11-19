package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_075_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("lembas", "4_75");
					put("lembas2", "4_75");

					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion; +3 only against a Nazgul
					// Legolas, Companion of the Ring: Elf companion; stronger only at a battleground or forest site
					put("legolas", "11_21");
					put("celeborn", "1_34");    // Celeborn, Lord of Lorien: Elf ally, cost 2; a fellowship ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion; a skirmish ability used only on purpose

					// Lorien Elf: an Elf companion whose only text is "To play, spot an Elf": the cards of a hand and a
					// draw deck to reconcile with
					put("elf1", "1_53");
					put("elf2", "1_53");
					put("elf3", "1_53");
					put("elf4", "1_53");
					put("elf5", "1_53");
					put("elf6", "1_53");

					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1; its play trigger does nothing when cheated in
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LembasStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Lembas
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype:
		 * Game Text: To play, spot 3 Elves. Bearer must be a companion.
		 * <b>Regroup:</b> Discard this possession to reconcile your hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("lembas");

		assertEquals("Lembas", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayableOnlyWithThreeElvesAndOnlyOnACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lembas = scn.GetFreepsCard("lembas");
		var frodo = scn.GetRingBearer();
		var arwen = scn.GetFreepsCard("arwen");
		var legolas = scn.GetFreepsCard("legolas");
		var celeborn = scn.GetFreepsCard("celeborn");
		var gimli = scn.GetFreepsCard("gimli");
		// Two Elf companions, and Gimli and Frodo, who are no Elves.
		scn.MoveCompanionsToTable(arwen, legolas, gimli);
		scn.MoveCardsToHand(lembas, celeborn);

		scn.StartGame();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertInHand(lembas, celeborn);
		assertFalse(scn.FreepsPlayAvailable(lembas));

		// An Elf ally is the third Elf.
		scn.FreepsPlayCard(celeborn);
		assertInZone(Zone.SUPPORT, celeborn);
		assertTrue(scn.FreepsPlayAvailable(lembas));

		scn.FreepsPlayCard(lembas);
		// Companions of three races and cultures, the Ring-bearer among them; not the ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, gimli, arwen, legolas));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(celeborn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gimli);
		assertAttachedTo(lembas, gimli);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RegroupAbilityDiscardsThisPossessionToReconcileTheHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lembas = scn.GetFreepsCard("lembas");
		var lembas2 = scn.GetFreepsCard("lembas2");
		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var elf1 = scn.GetFreepsCard("elf1");
		var elf2 = scn.GetFreepsCard("elf2");
		var elf3 = scn.GetFreepsCard("elf3");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, lembas);
		// A second copy on another companion.
		scn.AttachCardsTo(frodo, lembas2);

		scn.StartGame();
		assertAttachedTo(lembas, gimli);
		assertAttachedTo(lembas2, frodo);
		// No minion in play: the Shadow phase goes straight to regroup.
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveCardsToHand(elf1, elf2, elf3);
		scn.FreepsRefreshDecision();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(3, scn.GetFreepsHandCount());
		assertInHand(elf1, elf2, elf3);
		assertTrue(scn.FreepsActionAvailable(lembas));

		scn.FreepsUseCardAction(lembas);
		assertInDiscard(lembas);
		assertAttachedTo(lembas2, frodo);

		// The reconcile: first a discard of 0 or 1 cards, then a draw up to 8.
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(elf1);
		assertInDiscard(elf1);
		// 3 - 1 discarded + 6 drawn.
		assertEquals(8, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedInTheManeuverPhaseButIsInTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lembas = scn.GetFreepsCard("lembas");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, lembas);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		assertAttachedTo(lembas, gimli);
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(lembas));

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsActionAvailable(lembas));
	}
}
