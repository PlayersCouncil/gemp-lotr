package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_004_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bow", "18_4");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an [elven] companion, and Arwen
					put("haldir", "1_48");      // Haldir, Elf of the Golden Wood: an [elven] companion who is not Arwen, and no archer
					put("celeborn", "1_34");    // Celeborn, Lord of Lorien: an [elven] ally
					put("gimli", "1_13");       // Gimli, Son of Gloin: a companion of another culture

					// Cards for the hand.
					put("lorienelf", "1_53");   // Lorien Elf: an [elven] companion card
					put("sword1", "4_64");      // Elven Sword: an [elven] possession
					put("sword2", "4_64");
					put("axe", "1_9");          // Dwarven Axe: a [dwarven] card, no [elven] one

					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai, vitality 3, no other text
					put("runner", "1_178");     // Goblin Runner: a [moria] Orc, vitality 1, cheated in so its play trigger does nothing
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ArwensBowStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Arwen's Bow
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Ranged weapon
		 * Strength: +1
		 * Game Text: Bearer must be an [elven] companion.<br><b>Archery:</b> If bearer is Arwen, discard 2 [elven] cards from hand to wound a minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("bow");

		assertEquals("Arwen's Bow", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.RANGED_WEAPON));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void BearerMustBeAnElvenCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var arwen = scn.GetFreepsCard("arwen");
		var haldir = scn.GetFreepsCard("haldir");
		var celeborn = scn.GetFreepsCard("celeborn");
		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(arwen, haldir, gimli);
		scn.MoveAlliesToTable(celeborn);
		scn.MoveCardsToHand(bow);

		scn.StartGame();

		assertInHand(bow);
		assertTrue(scn.FreepsPlayAvailable(bow));
		scn.FreepsPlayCard(bow);

		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, haldir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(celeborn, gimli, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(haldir);

		assertAttachedTo(bow, haldir);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ArcheryAbilityDiscardsTwoElvenCardsFromHandToWoundAMinionWhileArwenBearsTheBow() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var arwen = scn.GetFreepsCard("arwen");
		var haldir = scn.GetFreepsCard("haldir");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		var sword1 = scn.GetFreepsCard("sword1");
		var sword2 = scn.GetFreepsCard("sword2");
		var celeborn = scn.GetFreepsCard("celeborn");
		var axe = scn.GetFreepsCard("axe");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(arwen, haldir);
		scn.AttachCardsTo(arwen, bow);
		scn.MoveCardsToHand(lorienelf, sword1, celeborn, sword2, axe);
		assertInHand(lorienelf, sword1, celeborn, sword2, axe);

		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, runner);
		scn.SkipToPhase(Phase.ARCHERY);

		assertAttachedTo(bow, arwen);
		assertEquals(5, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertTrue(scn.FreepsActionAvailable(bow));

		scn.FreepsUseCardAction(bow);
		// [elven] cards of three types may be discarded; the [dwarven] card may not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(lorienelf, sword1, celeborn, sword2));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(axe));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(lorienelf, sword1);

		assertInDiscard(lorienelf, sword1);
		assertEquals(3, scn.GetFreepsHandCount());
		// Either minion may be wounded; no companion may.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, runner));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(arwen, haldir, frodo));
		scn.FreepsChooseCard(savage);

		// One wound, of the three the Uruk could take.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(0, scn.GetWoundsOn(arwen));

		// Again, on the minion with one vitality: the wound kills it. The last two [elven] cards in hand go unasked.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertTrue(scn.FreepsActionAvailable(bow));
		scn.FreepsUseCardAction(bow);
		assertInDiscard(celeborn, sword2);
		assertInHand(axe);
		assertEquals(1, scn.GetFreepsHandCount());
		scn.FreepsChooseCard(runner);

		assertInZone(Zone.DISCARD, runner);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void ArcheryAbilityIsNotOfferedWhileTheBearerIsNotArwen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var arwen = scn.GetFreepsCard("arwen");
		var haldir = scn.GetFreepsCard("haldir");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		var sword1 = scn.GetFreepsCard("sword1");
		var sword2 = scn.GetFreepsCard("sword2");
		scn.MoveCompanionsToTable(arwen, haldir);
		// Arwen is on the table, but Haldir bears the Bow.
		scn.AttachCardsTo(haldir, bow);
		scn.MoveCardsToHand(lorienelf, sword1, sword2);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ARCHERY);

		assertAttachedTo(bow, haldir);
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(bow));
	}

	@Test
	public void ArcheryAbilityIsNotOfferedWithOnlyOneElvenCardInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var arwen = scn.GetFreepsCard("arwen");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(arwen);
		scn.AttachCardsTo(arwen, bow);
		// Two cards in hand, one of them [elven].
		scn.MoveCardsToHand(lorienelf, axe);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ARCHERY);

		assertAttachedTo(bow, arwen);
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(bow));
	}

	@Test
	public void ArcheryAbilityIsStillOfferedAndItsCostPaidOnceNoMinionIsLeftToWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var arwen = scn.GetFreepsCard("arwen");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		var sword1 = scn.GetFreepsCard("sword1");
		var sword2 = scn.GetFreepsCard("sword2");
		var celeborn = scn.GetFreepsCard("celeborn");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(arwen);
		scn.AttachCardsTo(arwen, bow);
		scn.MoveCardsToHand(lorienelf, sword1, celeborn, sword2, axe);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.ARCHERY);

		assertInHand(lorienelf, sword1, celeborn, sword2, axe);
		assertEquals(5, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertTrue(scn.FreepsActionAvailable(bow));

		// The only minion has one vitality: the wound kills it, and it is wounded without being asked.
		scn.FreepsUseCardAction(bow);
		scn.FreepsChooseCards(lorienelf, sword1);
		assertInDiscard(lorienelf, sword1);
		assertInZone(Zone.DISCARD, runner);
		assertEquals(3, scn.GetFreepsHandCount());

		// No minion is left, and the phase goes on: the Bow is offered all the same, and the cost is paid for
		// nothing.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertTrue(scn.FreepsActionAvailable(bow));
		scn.FreepsUseCardAction(bow);

		assertInDiscard(celeborn, sword2);
		assertInHand(axe);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}
}
