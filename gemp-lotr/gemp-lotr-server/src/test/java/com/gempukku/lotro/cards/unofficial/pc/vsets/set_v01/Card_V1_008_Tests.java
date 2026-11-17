package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_008_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("lament", "101_8");
					// Arwen, Daughter of Elrond: Elf companion, strength 6, vitality 3.  Her +3 is only against
					// a Nazgul.
					put("arwen", "1_30");
					// Celeborn, Lord of Lorien: Elf ally, vitality 3.  Fellowship: exert Celeborn to heal an
					// [elven] ally.
					put("celeborn", "1_34");
					// Aragorn, Ranger of the North: unbound [gondor] Man, strength 8, vitality 4
					put("aragorn", "1_89");
					// Gondorian Ranger: a [gondor] Man who is Ring-bound without being the Ring-bearer.  His
					// text only changes the site number of minions assigned to him.
					put("ranger", "4_122");
					// Gandalf, The Grey Wizard: vitality 4.  Fellowship: exert Gandalf to play a companion who
					// has the Gandalf signet.
					put("gandalf", "1_364");
					// Gimli, Son of Gloin: unique companion with the Gandalf signet, strength 6, vitality 3
					put("gimli", "1_13");
					put("boromir", "1_97");     // Boromir, Son of Denethor: unique companion
					put("guard", "1_7");        // Dwarf Guard: a companion that is not unique
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: a unique ally
					// The Last Alliance of Elves and Men: Free Peoples condition borne by a [gondor] Man
					put("alliance", "1_49");
					put("gilgalad", "1_66");    // The Tale of Gil-galad: condition borne by an Elf
					put("mallorn", "1_54");     // Mallorn-trees: condition in the support area
					put("coat", "1_101");       // Coat of Mail: possession borne by a Man

					// Desperate Defense of the Ring: Shadow condition borne by a companion.  Its text only
					// answers bearer being assigned to a skirmish.
					put("defense", "1_244");
					put("marksman", "1_176");   // Goblin Marksman: archer, vitality 1, no other text
					// Southron Bowman: vitality 2.  Archery: exert this minion to exert a companion (except the
					// Ring-bearer); it then does not add to the minion archery total.
					put("bowman", "4_248");
					// Hate: Maneuver event, (0): exert a [sauron] Orc to wound a companion (except the
					// Ring-bearer).
					put("hate", "1_250");
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, vitality 2; a skirmish ability
					// Ulaire Toldea: strength 12.  Assignment: spot 4 burdens to assign a companion (except the
					// Ring-bearer) to skirmish Ulaire Toldea.
					put("toldea", "1_236");
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3, damage +1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LamentForTheFallenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: Lament for the Fallen
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Condition
		 * Game Text: To play, exert an Elf and spot a unique companion in the dead pile.  Bearer must be an
		 * unbound companion.
		 * Bearer cannot be exerted, wounded, or assigned to a skirmish.
		 * At the start of the regroup phase, discard a condition on bearer (and heal bearer if Gandalf is in the
		 * dead pile).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("lament");

		assertEquals("Lament for the Fallen", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertFalse(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAChosenElfToPlayOnAChosenUnboundCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var lament = scn.GetFreepsCard("lament");
		var arwen = scn.GetFreepsCard("arwen");
		var celeborn = scn.GetFreepsCard("celeborn");
		var aragorn = scn.GetFreepsCard("aragorn");
		var ranger = scn.GetFreepsCard("ranger");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(arwen, aragorn, ranger);
		scn.MoveCardsToSupportArea(celeborn);
		scn.MoveCardsToDeadPile(boromir);
		scn.MoveCardsToHand(lament);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(celeborn));
		assertInHand(lament);
		assertTrue(scn.FreepsPlayAvailable(lament));

		scn.FreepsPlayCard(lament);
		// Bearer: an Elf or a Man, so long as the companion is unbound.  Not the Ring-bearer, not a Ring-bound
		// Man, not an ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, ranger, celeborn));
		scn.FreepsChooseCard(aragorn);

		// The Elf to exert: a companion or an ally, and it need not be the bearer.
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, celeborn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, aragorn, ranger));
		scn.FreepsChooseCard(celeborn);

		assertEquals(1, scn.GetWoundsOn(celeborn));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertAttachedTo(lament, aragorn);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheElfExertedToPlayItCanAlsoBeItsBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lament = scn.GetFreepsCard("lament");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(arwen, aragorn);
		scn.MoveCardsToDeadPile(boromir);
		scn.MoveCardsToHand(lament);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(arwen));
		assertTrue(scn.FreepsPlayAvailable(lament));

		scn.FreepsPlayCard(lament);
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, aragorn));
		scn.FreepsChooseCard(arwen);

		// Arwen is the only Elf, so she is exerted without asking: the cost is paid before she is its bearer.
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertAttachedTo(lament, arwen);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilAUniqueCompanionIsInMyDeadPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lament = scn.GetFreepsCard("lament");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var elrond = scn.GetFreepsCard("elrond");
		var boromir = scn.GetFreepsCard("boromir");
		var theirBoromir = scn.GetShadowCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(arwen);
		// A companion that is not unique and a unique ally in my dead pile, a unique companion in my discard
		// pile, and a unique companion in the opponent's dead pile.
		scn.MoveCardsToDeadPile(guard, elrond, theirBoromir);
		scn.MoveCardsToDiscard(boromir);
		scn.MoveCardsToHand(lament, aragorn);

		scn.StartGame();

		assertInZone(Zone.DEAD, guard, elrond, theirBoromir);
		assertFalse(scn.FreepsPlayAvailable(lament));

		// Gimli goes to my dead pile.  The game only looks again after an action, so Aragorn is played.
		scn.MoveCardsToDeadPile(gimli);
		scn.FreepsRefreshDecision();
		scn.FreepsPlayCard(aragorn);
		assertTrue(scn.FreepsPlayAvailable(lament));
		assertInHand(lament);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenNoElfIsAbleToExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lament = scn.GetFreepsCard("lament");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var celeborn = scn.GetFreepsCard("celeborn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(arwen, aragorn);
		scn.MoveCardsToDeadPile(boromir);
		scn.MoveCardsToHand(lament, celeborn);
		// Vitality 3 with 2 wounds: Arwen is exhausted.  Aragorn is fresh, but he is no Elf.
		scn.AddWoundsToChar(arwen, 2);

		scn.StartGame();

		assertTrue(scn.IsExhausted(arwen));
		assertFalse(scn.FreepsPlayAvailable(lament));

		// Once an Elf who can exert is in play, the same board lets it be played.
		scn.FreepsPlayCard(celeborn);
		assertTrue(scn.FreepsPlayAvailable(lament));
		assertInHand(lament);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BearerCanNoLongerExertToUseHisOwnAbility() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lament = scn.GetFreepsCard("lament");
		var arwen = scn.GetFreepsCard("arwen");
		var gandalf = scn.GetFreepsCard("gandalf");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(arwen, gandalf);
		scn.MoveCardsToDeadPile(boromir);
		// Gimli has the Gandalf signet, so Gandalf's ability has a companion to play.
		scn.MoveCardsToHand(lament, gimli);

		scn.StartGame();

		assertTrue(scn.FreepsActionAvailable(gandalf));

		// Arwen, the only Elf, is exerted for it.
		scn.FreepsPlayCard(lament);
		scn.FreepsChooseCard(gandalf);
		assertAttachedTo(lament, gandalf);

		assertFalse(scn.FreepsActionAvailable(gandalf));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertInHand(gimli);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ShadowCardCannotChooseBearerToExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var lament = scn.GetFreepsCard("lament");
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var gimli = scn.GetFreepsCard("gimli");
		var bowman = scn.GetShadowCard("bowman");
		scn.MoveCompanionsToTable(aragorn, arwen, gimli);
		scn.AttachCardsTo(aragorn, lament);

		scn.StartGame();

		scn.MoveMinionsToTable(bowman);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(bowman));
		scn.ShadowUseCardAction(bowman);
		assertEquals(1, scn.GetWoundsOn(bowman));
		// The Bowman's own text leaves out the Ring-bearer; the Lament leaves out Aragorn.
		assertTrue(scn.ShadowHasCardChoiceAvailable(arwen, gimli));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn, frodo));

		scn.ShadowChooseCard(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void BearerCannotBeWoundedByAShadowEventOrByArcheryFire() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var lament = scn.GetFreepsCard("lament");
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var gimli = scn.GetFreepsCard("gimli");
		var hate = scn.GetShadowCard("hate");
		var soldier = scn.GetShadowCard("soldier");
		var marksman = scn.GetShadowCard("marksman");
		scn.MoveCompanionsToTable(aragorn, arwen, gimli);
		scn.AttachCardsTo(aragorn, lament);
		scn.MoveCardsToHand(hate);

		scn.StartGame();

		scn.MoveMinionsToTable(soldier, marksman);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Hate exerts the Orc Soldier, the only [sauron] Orc, to wound a companion who is not the Ring-bearer.
		scn.ShadowPlayCard(hate);
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertTrue(scn.ShadowHasCardChoiceAvailable(arwen, gimli));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn, frodo));
		scn.ShadowChooseCard(arwen);
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// Archery fire: the Goblin Marksman's one arrow can go to anyone in the fellowship but Aragorn.
		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassArcheryActions();
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, arwen, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		scn.FreepsChooseCard(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void BearerCannotBeAssignedToASkirmishByAnAssignmentActionOrByEitherPlayer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var lament = scn.GetFreepsCard("lament");
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var gimli = scn.GetFreepsCard("gimli");
		var toldea = scn.GetShadowCard("toldea");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(aragorn, arwen, gimli);
		scn.AttachCardsTo(aragorn, lament);

		scn.StartGame();

		// Ulaire Toldea's ability spots 4 burdens.
		scn.AddBurdens(4);
		scn.MoveMinionsToTable(toldea, troop, savage);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// An assignment action: Toldea's own text leaves out the Ring-bearer; the Lament leaves out Aragorn.
		scn.ShadowUseCardAction(toldea);
		assertTrue(scn.ShadowHasCardChoiceAvailable(arwen, gimli));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn, frodo));
		scn.ShadowChooseCard(gimli);
		assertTrue(scn.IsCharAssignedAgainst(gimli, toldea));

		// The Free Peoples player's assignments: Frodo and Arwen are free to defend, Gimli is taken, and
		// Aragorn cannot be assigned.
		scn.PassAssignmentActions();
		assertEquals(2, scn.FreepsGetFreepsAssignmentTargetCount());
		assertTrue(scn.FreepsCanAssign(frodo));
		assertTrue(scn.FreepsCanAssign(arwen));
		assertFalse(scn.FreepsCanAssign(aragorn));
		scn.FreepsAssignToMinions(arwen, troop);

		// The Shadow player's leftover Uruk Savage may go to any companion, assigned already or not, but
		// Aragorn.
		assertEquals(3, scn.ShadowGetFreepsAssignmentTargetCount());
		scn.ShadowAssignToMinions(arwen, savage);

		assertTrue(scn.IsCharAssignedAgainst(arwen, troop));
		assertTrue(scn.IsCharAssignedAgainst(arwen, savage));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void AtTheStartOfRegroupDiscardsItselfWhenItIsTheOnlyConditionOnBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lament = scn.GetFreepsCard("lament");
		var aragorn = scn.GetFreepsCard("aragorn");
		var coat = scn.GetFreepsCard("coat");
		var boromir = scn.GetFreepsCard("boromir");
		var gandalf = scn.GetFreepsCard("gandalf");
		var theirGandalf = scn.GetShadowCard("gandalf");
		scn.MoveCompanionsToTable(aragorn);
		// Aragorn also bears a possession, which is not a condition.
		scn.AttachCardsTo(aragorn, lament, coat);
		scn.AddWoundsToChar(aragorn, 2);
		// Boromir is in my dead pile, and Gandalf is not: he is in my discard pile and in the opponent's dead
		// pile.
		scn.MoveCardsToDeadPile(boromir, theirGandalf);
		scn.MoveCardsToDiscard(gandalf);

		scn.StartGame();

		// With no minion in play the Shadow phase is the last thing before the regroup phase.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertAttachedTo(lament, aragorn);

		scn.ShadowPass();
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertInDiscard(lament);
		assertAttachedTo(coat, aragorn);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void OwnerChoosesWhichConditionOnBearerToDiscardAndBearerIsHealedWithGandalfInTheDeadPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lament = scn.GetFreepsCard("lament");
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var alliance = scn.GetFreepsCard("alliance");
		var coat = scn.GetFreepsCard("coat");
		var gilgalad = scn.GetFreepsCard("gilgalad");
		var mallorn = scn.GetFreepsCard("mallorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var defense = scn.GetShadowCard("defense");
		scn.MoveCompanionsToTable(aragorn, arwen);
		// On Aragorn: the Lament, another Free Peoples condition, a Shadow condition, and a possession.
		// Elsewhere: a condition on Arwen and a condition in the support area.
		scn.AttachCardsTo(aragorn, lament, alliance, defense, coat);
		scn.AttachCardsTo(arwen, gilgalad);
		scn.MoveCardsToSupportArea(mallorn);
		scn.AddWoundsToChar(aragorn, 2);
		scn.MoveCardsToDeadPile(gandalf);

		scn.StartGame();

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetWoundsOn(aragorn));

		scn.ShadowPass();
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.FreepsHasCardChoiceAvailable(lament, alliance, defense));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(coat, gilgalad, mallorn));

		scn.FreepsChooseCard(defense);
		assertInDiscard(defense);
		assertAttachedTo(lament, aragorn);
		assertAttachedTo(alliance, aragorn);
		assertAttachedTo(coat, aragorn);
		assertAttachedTo(gilgalad, arwen);
		assertInZone(Zone.SUPPORT, mallorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void BearerIsHealedWithGandalfInTheDeadPileEvenWhenItDiscardsItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lament = scn.GetFreepsCard("lament");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, lament);
		scn.AddWoundsToChar(aragorn, 2);
		scn.MoveCardsToDeadPile(gandalf);

		scn.StartGame();

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertAttachedTo(lament, aragorn);
		assertEquals(2, scn.GetWoundsOn(aragorn));

		scn.ShadowPass();
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertInDiscard(lament);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
