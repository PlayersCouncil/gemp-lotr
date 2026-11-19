package com.gempukku.lotro.cards.unofficial.hobbit.set33;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_33_021_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. The skirmishes of
	// turn 1 are at The Troll Hoard (site 2).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("blade", "33_21");
					// Gandalf, Friend of Thorin: [gandalf] Wizard companion; a cost modifier on Thorin and a fellowship
					// ability only (not Gandalf, The Grey, whose trigger answers the play of a [gandalf] event)
					put("gandalf", "30_26");
					// Glamdring: [gandalf] artifact, hand weapon, "Bearer must be Gandalf"; a trigger on his wins only
					put("glamdring", "30_29");
					// Glamdring: the Decipher Glamdring, a [gandalf] possession, "Bearer must be Gandalf"; an ability
					// used only on purpose
					put("glamdring1", "1_75");
					// Glamdring, Foe-hammer: another Decipher Glamdring, a [gandalf] possession; a trigger on his wins only
					put("glamdring11", "11_35");
					// Thorin, Oakenshield: [dwarven] Dwarf companion; strength 7, +1 for each [dwarven] follower (none)
					put("thorin", "30_19");
					// Orcrist: [dwarven] artifact hand weapon on Thorin, strength +2; a trigger on his wins (wound an Orc)
					put("orcrist", "30_17");
					// Elrond, Elven Lord: an [elven] Elf companion, strength 8; "To play, spot Aragorn or an Elf" (cheated
					// in) and a start-of-turn trigger, which no test reaches
					put("elrond", "7_21");
					// Hadafang: [elven] artifact hand weapon, "Bearer must be Elrond", strength +2; a fellowship ability
					put("hadafang", "33_13");
					// Elrond, Elven Lord: the Hobbit-set Elrond, an [elven] Elf ally, home 3H; a start-of-turn heal of
					// a wounded Wise ally and a regroup ability only
					put("elrondally", "30_21");
					// Dwalin, Brother of Balin: [dwarven] Dwarf companion, strength 7; a maneuver ability only
					put("dwalin", "30_9");
					// Dwarven Axe: a [dwarven] hand weapon that is a possession, strength +2; damage +1 at a mountain
					put("axe", "31_1");
					// Mithril Coat: [dwarven] artifact, Armor, "Bearer must be Bilbo": an artifact that is no weapon
					put("coat", "31_5");
					// Wizard Staff: a [gandalf] artifact of another title; "Bearer must be a Wizard"
					put("staff", "31_18");

					// Goblin Footman: [moria] Orc, strength 8, vitality 1; a play trigger only
					put("footman", "30_42");
					// Dark Bats: [moria] Creature, strength 4, vitality 2; a Shadow ability only
					put("bats", "32_30");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void NoFinerBladeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: No Finer Blade
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Choose one: make a companion bearing an artifact weapon strength +3; or play Glamdring from your draw deck or discard pile.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("blade");

		assertEquals("No Finer Blade", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithBothChoicesOpenTheStrengthGoesToACompanionBearingAnArtifactWeaponForTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blade = scn.GetFreepsCard("blade");
		var bilbo = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring = scn.GetFreepsCard("glamdring");
		var thorin = scn.GetFreepsCard("thorin");
		var orcrist = scn.GetFreepsCard("orcrist");
		var elrond = scn.GetFreepsCard("elrond");
		var hadafang = scn.GetFreepsCard("hadafang");
		var dwalin = scn.GetFreepsCard("dwalin");
		var axe = scn.GetFreepsCard("axe");
		var coat = scn.GetFreepsCard("coat");
		var footman = scn.GetShadowCard("footman");
		var bats = scn.GetShadowCard("bats");

		scn.StartGame();
		scn.MoveCompanionsToTable(gandalf, thorin, elrond, dwalin);
		scn.AttachCardsTo(thorin, orcrist);
		scn.AttachCardsTo(elrond, hadafang);
		scn.AttachCardsTo(dwalin, axe);
		scn.AttachCardsTo(bilbo, coat);
		scn.MoveCardsToHand(blade);
		scn.MoveCardsToDiscard(glamdring);
		scn.MoveMinionsToTable(footman, bats);
		assertAttachedTo(orcrist, thorin);
		assertAttachedTo(hadafang, elrond);
		assertAttachedTo(axe, dwalin);
		assertAttachedTo(coat, bilbo);
		assertInZone(Zone.DISCARD, glamdring);
		int shuffles = scn.GetFreepsDeckShuffleCount();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{thorin, footman}, new PhysicalCardImpl[]{dwalin, bats});
		scn.FreepsResolveSkirmish(thorin);
		// 7 + 2 for Orcrist; 8 + 2 for Hadafang.
		assertEquals(9, scn.GetStrength(thorin));
		assertEquals(10, scn.GetStrength(elrond));
		assertEquals(9, scn.GetStrength(dwalin));

		assertTrue(scn.FreepsPlayAvailable(blade));
		scn.FreepsPlayCard(blade);
		// A companion bearing an artifact weapon, and a Glamdring in the discard pile with Gandalf to bear it: asked.
		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		scn.FreepsChoose("strength");

		// Thorin, skirmishing, and Elrond, in no skirmish; not Dwalin, whose weapon is a possession, nor Bilbo, whose
		// artifact is no weapon, nor Gandalf, who bears nothing.
		assertTrue(scn.FreepsHasCardChoiceAvailable(thorin, elrond));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(dwalin, bilbo, gandalf));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(elrond);

		assertEquals(13, scn.GetStrength(elrond));
		assertEquals(9, scn.GetStrength(thorin));
		assertEquals(9, scn.GetStrength(dwalin));
		assertInZone(Zone.DISCARD, blade, glamdring);
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Thorin, 9, beats the Footman, 8; in the next skirmish Elrond's +3 is gone.
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, footman);
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		scn.FreepsResolveSkirmish(dwalin);
		assertEquals(10, scn.GetStrength(elrond));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithGlamdringAlreadyBorneTheStrengthIsMadeWithNoChoiceOfWhatToDo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blade = scn.GetFreepsCard("blade");
		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring = scn.GetFreepsCard("glamdring");
		var glamdring1 = scn.GetFreepsCard("glamdring1");
		var glamdring11 = scn.GetFreepsCard("glamdring11");
		var thorin = scn.GetFreepsCard("thorin");
		var orcrist = scn.GetFreepsCard("orcrist");
		var dwalin = scn.GetFreepsCard("dwalin");
		var axe = scn.GetFreepsCard("axe");
		var footman = scn.GetShadowCard("footman");
		var bats = scn.GetShadowCard("bats");

		scn.StartGame();
		scn.MoveCompanionsToTable(gandalf, thorin, dwalin);
		scn.AttachCardsTo(gandalf, glamdring);
		scn.AttachCardsTo(thorin, orcrist);
		scn.AttachCardsTo(dwalin, axe);
		scn.MoveCardsToHand(blade);
		scn.SetFreepsDrawDeck(glamdring1);
		scn.MoveCardsToDiscard(glamdring11);
		scn.MoveMinionsToTable(footman, bats);
		assertAttachedTo(glamdring, gandalf);
		assertAttachedTo(orcrist, thorin);
		assertAttachedTo(axe, dwalin);
		assertInZone(Zone.DECK, glamdring1);
		assertInZone(Zone.DISCARD, glamdring11);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gandalf, footman}, new PhysicalCardImpl[]{dwalin, bats});
		scn.FreepsResolveSkirmish(gandalf);
		assertEquals(9, scn.GetStrength(gandalf));
		assertEquals(9, scn.GetStrength(thorin));

		scn.FreepsPlayCard(blade);
		// Glamdring is unique and already borne: no Glamdring can be played from either pile, so the strength is
		// what is done, with no choice of what to do.
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		// Gandalf, skirmishing, and Thorin, in no skirmish; not Dwalin, whose weapon is a possession.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, thorin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(dwalin));
		scn.FreepsChooseCard(gandalf);

		assertEquals(12, scn.GetStrength(gandalf));
		assertEquals(9, scn.GetStrength(thorin));
		assertInZone(Zone.DECK, glamdring1);
		assertInZone(Zone.DISCARD, glamdring11);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoGlamdringInEitherPileTheStrengthIsMadeWithNoChoiceOfWhatToDo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blade = scn.GetFreepsCard("blade");
		var gandalf = scn.GetFreepsCard("gandalf");
		var thorin = scn.GetFreepsCard("thorin");
		var orcrist = scn.GetFreepsCard("orcrist");
		var staff = scn.GetFreepsCard("staff");
		var footman = scn.GetShadowCard("footman");

		scn.StartGame();
		scn.MoveCompanionsToTable(gandalf, thorin);
		scn.AttachCardsTo(thorin, orcrist);
		scn.MoveCardsToHand(blade);
		scn.SetFreepsDrawDeck(staff);
		scn.MoveMinionsToTable(footman);
		assertAttachedTo(orcrist, thorin);
		assertInZone(Zone.DECK, staff);
		int shuffles = scn.GetFreepsDeckShuffleCount();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thorin, footman);
		scn.FreepsResolveSkirmish(thorin);
		assertEquals(9, scn.GetStrength(thorin));

		scn.FreepsPlayCard(blade);
		// Gandalf could bear a Glamdring, but there is none in either pile: Thorin, the one companion bearing an
		// artifact weapon, is made strength +3 with nothing asked and no look through the draw deck.
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(12, scn.GetStrength(thorin));
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoGandalfToBearGlamdringTheStrengthIsMadeWithNoChoiceOfWhatToDo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blade = scn.GetFreepsCard("blade");
		var glamdring = scn.GetFreepsCard("glamdring");
		var glamdring11 = scn.GetFreepsCard("glamdring11");
		var thorin = scn.GetFreepsCard("thorin");
		var orcrist = scn.GetFreepsCard("orcrist");
		var footman = scn.GetShadowCard("footman");

		scn.StartGame();
		scn.MoveCompanionsToTable(thorin);
		scn.AttachCardsTo(thorin, orcrist);
		scn.MoveCardsToHand(blade);
		scn.SetFreepsDrawDeck(glamdring);
		scn.MoveCardsToDiscard(glamdring11);
		scn.MoveMinionsToTable(footman);
		assertAttachedTo(orcrist, thorin);
		assertInZone(Zone.DECK, glamdring);
		assertInZone(Zone.DISCARD, glamdring11);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thorin, footman);
		scn.FreepsResolveSkirmish(thorin);
		assertEquals(9, scn.GetStrength(thorin));

		scn.FreepsPlayCard(blade);
		// A Glamdring in each pile and no Gandalf to bear one: Thorin is made strength +3 with nothing asked.
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(12, scn.GetStrength(thorin));
		assertInZone(Zone.DECK, glamdring);
		assertInZone(Zone.DISCARD, glamdring11);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithAGlamdringInEachPileThePileIsAskedAndGlamdringIsPlayedFromTheDiscardPileOnGandalf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blade = scn.GetFreepsCard("blade");
		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring = scn.GetFreepsCard("glamdring");
		var glamdring1 = scn.GetFreepsCard("glamdring1");
		var glamdring11 = scn.GetFreepsCard("glamdring11");
		var orcrist = scn.GetFreepsCard("orcrist");
		var dwalin = scn.GetFreepsCard("dwalin");
		var axe = scn.GetFreepsCard("axe");
		var hisGlamdring = scn.GetShadowCard("glamdring1");
		var bats = scn.GetShadowCard("bats");

		scn.StartGame();
		scn.MoveCompanionsToTable(gandalf, dwalin);
		scn.AttachCardsTo(dwalin, axe);
		scn.MoveCardsToHand(blade);
		scn.SetFreepsDrawDeck(glamdring1);
		scn.MoveCardsToDiscard(glamdring, glamdring11, orcrist, hisGlamdring);
		scn.MoveMinionsToTable(bats);
		assertAttachedTo(axe, dwalin);
		assertInZone(Zone.DECK, glamdring1);
		assertInZone(Zone.DISCARD, glamdring, glamdring11, orcrist, hisGlamdring);
		int shuffles = scn.GetFreepsDeckShuffleCount();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(dwalin, bats);
		scn.FreepsResolveSkirmish(dwalin);

		scn.FreepsPlayCard(blade);
		// No companion bears an artifact weapon (Dwalin's is a possession); a Glamdring in each pile: the pile is asked.
		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		scn.FreepsChoose("discard");

		// The artifact Glamdring and a Decipher possession Glamdring; not Orcrist, nor the other player's Glamdring in
		// his own discard pile (a Decipher Glamdring, a blueprint this pile does not hold: a choice from a pile is read
		// by blueprint).
		assertTrue(scn.FreepsHasCardChoiceAvailable(glamdring, glamdring11));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(orcrist));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(hisGlamdring));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(glamdring11);

		assertAttachedTo(glamdring11, gandalf);
		assertInZone(Zone.DISCARD, glamdring, orcrist, hisGlamdring, blade);
		assertInZone(Zone.DECK, glamdring1);
		// No look through the draw deck.
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoCompanionBearingAnArtifactWeaponGlamdringIsPlayedFromTheDrawDeckUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blade = scn.GetFreepsCard("blade");
		var bilbo = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring = scn.GetFreepsCard("glamdring");
		var glamdring1 = scn.GetFreepsCard("glamdring1");
		var orcrist = scn.GetFreepsCard("orcrist");
		var staff = scn.GetFreepsCard("staff");
		var elrondally = scn.GetFreepsCard("elrondally");
		var hadafang = scn.GetFreepsCard("hadafang");
		var dwalin = scn.GetFreepsCard("dwalin");
		var axe = scn.GetFreepsCard("axe");
		var coat = scn.GetFreepsCard("coat");
		var bats = scn.GetShadowCard("bats");

		scn.StartGame();
		scn.MoveCompanionsToTable(gandalf, dwalin);
		scn.MoveAlliesToTable(elrondally);
		scn.AttachCardsTo(elrondally, hadafang);
		scn.AttachCardsTo(dwalin, axe);
		scn.AttachCardsTo(bilbo, coat);
		scn.MoveCardsToHand(blade);
		scn.SetFreepsDrawDeck(glamdring, glamdring1, staff, orcrist);
		scn.MoveMinionsToTable(bats);
		assertAttachedTo(hadafang, elrondally);
		assertAttachedTo(axe, dwalin);
		assertAttachedTo(coat, bilbo);
		assertInZone(Zone.DECK, glamdring, glamdring1, staff, orcrist);
		int shuffles = scn.GetFreepsDeckShuffleCount();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(dwalin, bats);
		scn.FreepsResolveSkirmish(dwalin);
		assertEquals(10, scn.GetStrength(elrondally));

		scn.FreepsPlayCard(blade);
		// The ally Elrond bears an artifact weapon, but he is no companion; Dwalin's weapon is a possession and
		// Bilbo's artifact is no weapon. The discard pile is empty: the draw deck is looked through, unasked.
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		scn.FreepsDismissRevealedCards();
		// The artifact Glamdring and a Decipher possession Glamdring; not Wizard Staff or Orcrist.
		assertTrue(scn.FreepsHasCardChoiceAvailable(glamdring, glamdring1));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(staff, orcrist));
		scn.FreepsChooseCardBPFromSelection(glamdring);

		assertAttachedTo(glamdring, gandalf);
		assertInZone(Zone.DECK, glamdring1, staff, orcrist);
		assertEquals(10, scn.GetStrength(elrondally));
		// Shuffled as the card leaves the deck looked through, and again after the play.
		assertEquals(shuffles + 2, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithGlamdringInTheDiscardPileAloneItIsPlayedFromThereUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blade = scn.GetFreepsCard("blade");
		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring1 = scn.GetFreepsCard("glamdring1");
		var glamdring11 = scn.GetFreepsCard("glamdring11");
		var staff = scn.GetFreepsCard("staff");
		var dwalin = scn.GetFreepsCard("dwalin");
		var axe = scn.GetFreepsCard("axe");
		var bats = scn.GetShadowCard("bats");

		scn.StartGame();
		scn.MoveCompanionsToTable(gandalf, dwalin);
		scn.AttachCardsTo(dwalin, axe);
		scn.MoveCardsToHand(blade);
		scn.SetFreepsDrawDeck(staff);
		scn.MoveCardsToDiscard(glamdring1, glamdring11);
		scn.MoveMinionsToTable(bats);
		assertAttachedTo(axe, dwalin);
		assertInZone(Zone.DECK, staff);
		assertInZone(Zone.DISCARD, glamdring1, glamdring11);
		int shuffles = scn.GetFreepsDeckShuffleCount();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(dwalin, bats);
		scn.FreepsResolveSkirmish(dwalin);

		scn.FreepsPlayCard(blade);
		// The draw deck holds no Glamdring and no companion bears an artifact weapon: the discard pile, unasked.
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(glamdring1, glamdring11));
		scn.FreepsChooseCard(glamdring1);

		assertAttachedTo(glamdring1, gandalf);
		assertInZone(Zone.DISCARD, glamdring11);
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNothingToDoOnEitherChoiceTheEventIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blade = scn.GetFreepsCard("blade");
		var gandalf = scn.GetFreepsCard("gandalf");
		var staff = scn.GetFreepsCard("staff");
		var dwalin = scn.GetFreepsCard("dwalin");
		var axe = scn.GetFreepsCard("axe");
		var bats = scn.GetShadowCard("bats");

		scn.StartGame();
		scn.MoveCompanionsToTable(gandalf, dwalin);
		scn.AttachCardsTo(dwalin, axe);
		scn.MoveCardsToHand(blade);
		scn.SetFreepsDrawDeck(staff);
		scn.MoveMinionsToTable(bats);
		assertAttachedTo(axe, dwalin);
		assertInZone(Zone.DECK, staff);
		int shuffles = scn.GetFreepsDeckShuffleCount();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(dwalin, bats);
		scn.FreepsResolveSkirmish(dwalin);
		assertEquals(9, scn.GetStrength(dwalin));

		// No companion bears an artifact weapon and there is no Glamdring in either pile: the event is played for
		// nothing, with nothing asked and no look through the draw deck.
		assertTrue(scn.FreepsPlayAvailable(blade));
		scn.FreepsPlayCard(blade);
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertInZone(Zone.DISCARD, blade);
		assertEquals(9, scn.GetStrength(dwalin));
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ItIsNotPlayedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blade = scn.GetFreepsCard("blade");
		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring = scn.GetFreepsCard("glamdring");
		var thorin = scn.GetFreepsCard("thorin");
		var orcrist = scn.GetFreepsCard("orcrist");
		var footman = scn.GetShadowCard("footman");

		scn.StartGame();
		scn.MoveCompanionsToTable(gandalf, thorin);
		scn.AttachCardsTo(thorin, orcrist);
		scn.MoveCardsToHand(blade);
		scn.MoveCardsToDiscard(glamdring);
		scn.MoveMinionsToTable(footman);
		assertAttachedTo(orcrist, thorin);
		assertInZone(Zone.DISCARD, glamdring);

		// Both choices could be done, but this is the Free Peoples player's assignment phase.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertInHand(blade);
		assertFalse(scn.FreepsPlayAvailable(blade));
	}
}
