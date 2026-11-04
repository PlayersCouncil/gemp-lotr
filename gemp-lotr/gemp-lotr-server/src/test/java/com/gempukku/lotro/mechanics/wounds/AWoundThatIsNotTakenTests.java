package com.gempukku.lotro.mechanics.wounds;

import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * A wound that is taken, and a wound that is not.
 *
 * Comprehensive Rules 4.2, "wound": "Wounds are always placed on a character one at a time.  When you 'wound a
 * character,' you place only one wound."  And: "if a card prevents wounds, wounds may still be assigned to that
 * character."  "Preventing effects" is the rules' entry for one effect stopping another.
 *
 * A wound that lands is one token, and whatever answers "takes a wound" answers it once.  A wound that is
 * prevented, or whose character has left play before it lands, is no token and is answered by nothing; and a
 * wound prevented for one character leaves another, wounded by the same effect, to take his.  An effect that
 * wounds several characters wounds them one at a time, each with its own "is about to take a wound", in the
 * order they came into play (Ketura on Q-0142; TakenTogetherInCardIdOrderTests).
 *
 * What answers a wound taken in these tests is Rank and File, the one Decipher card whose text is set off by a
 * minion taking a wound in any phase; so the wounded are roaming [sauron] minions.
 *
 * An exertion places a wound token too, and it is no wound taken: CR 4.2, "exert": "To exert a character, place a
 * wound token on that character"; "wound" is a separate act.  Nothing that answers a wound taken, or one about to
 * be taken, answers an exertion.  That is shown once, here (Ketura on Q-0265, 2026-10-12: "A"), and not card by
 * card; The One Ring 1_1 and 7_2 read it in their own files as well.
 */
public class AWoundThatIsNotTakenTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Rank and File, a [sauron] condition of the support area: "Each time a roaming [sauron]
					// minion takes a wound, you may add a [sauron] token here."  Its two abilities are never used.
					put("rank", "10_96");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man, vitality 4
					// Defend It and Hope: "Maneuver: Exert a [gondor] Man to wound a minion once (or twice if
					// that minion is an Uruk-hai)."
					put("defendit", "4_115");
					// Orc Inquisitor: a [sauron] Orc of site 6, vitality 3; its text is about being played.
					put("inquisitor", "1_268");

					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf, strength 6, vitality 3; damage +1
					// Here Lies Balin, Son of Fundin: "Maneuver: Exert a Dwarf to wound 2 Orcs or to wound 1 Orc
					// twice."
					put("balin", "1_19");
					// Gorgoroth Soldier: a [sauron] Orc of site 5, vitality 2; besieger.  "Response: If a
					// besieger is about to take a wound, discard 2 cards from hand to prevent it."
					put("soldier1", "7_278");
					put("soldier2", "7_278");
					put("hand1", "1_177");      // Goblin Patrol Troop: four cards for the Shadow player to hold
					put("hand2", "1_177");
					put("hand3", "1_177");
					put("hand4", "1_177");

					// With All Possible Speed: "Response: If your hunter minion is about to take a wound during a
					// skirmish, discard that minion to exert a non-hunter companion."  Its other text is about
					// [uruk-hai] minions.
					put("speed", "15_180");
					// Gate Trooper: a [sauron] Orc of site 6, strength 5, vitality 2; its text is about being
					// played.
					put("trooper", "5_98");

					// Seeking Its Master: a [sauron] condition of the support area.  "Shadow: Exert a [sauron] Orc and
					// remove (2) to draw a card."
					put("seeking", "1_276");
					// Gorgoroth Troop: a [sauron] Orc of site 5, vitality 4; besieger.  Its one ability, a skirmish
					// ability that stacks a besieger on a controlled site, is never used.
					put("troop", "7_279");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AWoundThatNothingStopsIsOneTokenAndIsAnsweredOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var defendit = scn.GetFreepsCard("defendit");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defendit);

		var rank = scn.GetShadowCard("rank");
		var inquisitor = scn.GetShadowCard("inquisitor");
		scn.MoveCardsToSupportArea(rank);

		scn.StartGame();
		scn.MoveMinionsToTable(inquisitor);
		// One wound already, so that a second token would show beside a first.
		scn.AddWoundsToChar(inquisitor, 1);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.HasKeyword(inquisitor, Keyword.ROAMING));
		assertEquals(1, scn.GetWoundsOn(inquisitor));
		assertEquals(0, scn.GetCultureTokensOn(rank));

		// The Inquisitor is the only minion, and no Uruk-hai: one wound.
		scn.FreepsPlayCard(defendit);
		assertEquals(2, scn.GetWoundsOn(inquisitor));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(rank));

		// The wound taken is answered once, and not again.
		scn.ShadowChooseOptionalTrigger(rank);
		assertEquals(1, scn.GetCultureTokensOn(rank));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(rank));
		assertEquals(2, scn.GetWoundsOn(inquisitor));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AWoundPreventedForOneCharacterIsNotTakenOrAnsweredAndTheOtherWoundedWithHimTakesHis() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(balin);

		var rank = scn.GetShadowCard("rank");
		var soldier1 = scn.GetShadowCard("soldier1");
		var soldier2 = scn.GetShadowCard("soldier2");
		var hand1 = scn.GetShadowCard("hand1");
		var hand2 = scn.GetShadowCard("hand2");
		scn.MoveCardsToSupportArea(rank);
		// Four cards: enough to pay for the prevention twice, so that the second wound is let through by
		// choice and not for want of cards.
		scn.MoveCardsToHand(hand1, hand2, scn.GetShadowCard("hand3"), scn.GetShadowCard("hand4"));

		scn.StartGame();
		// One at a time: the first to come into play is the first to be wounded.
		scn.MoveMinionsToTable(soldier1);
		scn.MoveMinionsToTable(soldier2);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.HasKeyword(soldier1, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(soldier2, Keyword.ROAMING));
		assertEquals(4, scn.GetShadowHandCount());

		// One effect wounds both Soldiers, the only two Orcs.
		scn.FreepsPlayCard(balin);
		scn.FreepsChoose("Wound 2 Orcs");

		// The first Soldier is about to take his wound, and the response is used for him: 2 cards from hand.
		assertEquals(0, scn.GetWoundsOn(soldier1));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(soldier1));
		scn.ShadowChooseOptionalTrigger(soldier1);
		assertEquals(2, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCards(hand1, hand2);
		assertInZone(Zone.DISCARD, hand1);
		assertInZone(Zone.DISCARD, hand2);
		assertEquals(2, scn.GetShadowHandCount());

		// His wound is not taken and nothing answers it: the game has gone on to the second Soldier's wound,
		// with Rank and File not offered and no token on it.
		assertEquals(0, scn.GetWoundsOn(soldier1));
		assertEquals(0, scn.GetWoundsOn(soldier2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(rank));
		assertEquals(0, scn.GetCultureTokensOn(rank));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(soldier2));

		// The second Soldier's is let through.  The prevention of the first did not save him: he takes his,
		// and that wound is answered.
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(soldier2));
		assertEquals(0, scn.GetWoundsOn(soldier1));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(rank));

		// Answered once, for the one wound taken.
		scn.ShadowChooseOptionalTrigger(rank);
		assertEquals(1, scn.GetCultureTokensOn(rank));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(rank));
		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AWoundToACharacterWhoHasLeftPlayIsNotTakenAndNothingAnswersIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var defendit = scn.GetFreepsCard("defendit");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveCardsToHand(defendit);

		var rank = scn.GetShadowCard("rank");
		var speed = scn.GetShadowCard("speed");
		var trooper = scn.GetShadowCard("trooper");
		scn.MoveCardsToSupportArea(rank, speed);

		scn.StartGame();
		scn.MoveMinionsToTable(trooper);
		// A hunter who is a roaming [sauron] minion, so that Rank and File answers a wound he takes: no
		// [sauron] minion is printed with hunter, but Saruman, Instigator of Insurrection (17_37) gives every
		// minion hunter 2.  Hunter 2 makes his 5 a 7 against Gimli, who is given the strength to beat him.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, trooper, null, Keyword.HUNTER, 2));
		scn.ApplyAdHocModifier(new StrengthModifier(null, gimli, null, 3));
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.HasKeyword(trooper, Keyword.ROAMING));
		assertEquals(0, scn.GetWoundsOn(trooper));
		assertEquals(0, scn.GetCultureTokensOn(rank));

		// The control: a wound the Trooper does take, in the maneuver phase, is answered by Rank and File.
		scn.FreepsPlayCard(defendit);
		assertEquals(1, scn.GetWoundsOn(trooper));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(rank));
		scn.ShadowChooseOptionalTrigger(rank);
		assertEquals(1, scn.GetCultureTokensOn(rank));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, trooper);
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(9, scn.GetStrength(gimli));
		assertEquals(7, scn.GetStrength(trooper));

		// Gimli wins, and is damage +1: two wounds are due to the Trooper.  As he is about to take the first
		// the response is used.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(trooper));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(speed));
		scn.ShadowChooseOptionalTrigger(speed);

		// He is discarded as its cost, before the wound lands, and a companion who is no hunter exerts.
		assertInZone(Zone.DISCARD, trooper);
		assertTrue(scn.ShadowHasCardChoiceAvailable(gimli, frodo, aragorn));
		scn.ShadowChooseCard(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));

		// Neither of the two wounds is taken by a card that is no longer in play, and nothing answers one:
		// Rank and File is not offered and has the one token it had, nobody is asked anything about the
		// second wound, and the game has gone on to the regroup phase.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(rank));
		assertEquals(1, scn.GetCultureTokensOn(rank));
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnExertionIsNoWoundTakenAndNothingThatAnswersAWoundAnswersIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var defendit = scn.GetFreepsCard("defendit");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defendit);

		var rank = scn.GetShadowCard("rank");
		var seeking = scn.GetShadowCard("seeking");
		var troop = scn.GetShadowCard("troop");
		var soldier1 = scn.GetShadowCard("soldier1");
		scn.MoveCardsToSupportArea(rank, seeking);
		// Two cards: enough to pay for the Soldier's prevention.
		scn.MoveCardsToHand(scn.GetShadowCard("hand1"), scn.GetShadowCard("hand2"));

		scn.StartGame();
		// The Troop, a roaming [sauron] besieger with 4 vitality, is the one exerted and then wounded.  The
		// Soldier's response answers any besieger about to take a wound, Rank and File any roaming [sauron] minion
		// that takes one.
		scn.MoveMinionsToTable(troop, soldier1);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.HasKeyword(troop, Keyword.ROAMING));
		assertEquals(0, scn.GetWoundsOn(troop));

		// The Troop exerts for Seeking Its Master: a wound token is placed on him.
		scn.ShadowUseCardAction(seeking);
		scn.ShadowChooseCard(troop);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(soldier1));

		// No wound was about to be taken and none was taken: the Soldier's response is not offered, nor is Rank
		// and File, and the Shadow player is back at his phase actions.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(soldier1));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(rank));
		assertEquals(0, scn.GetCultureTokensOn(rank));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The control: a wound to the same Troop is answered by both.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPlayCard(defendit);
		scn.FreepsChooseCard(troop);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(soldier1));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(troop));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(rank));
		scn.ShadowChooseOptionalTrigger(rank);
		assertEquals(1, scn.GetCultureTokensOn(rank));
	}
}
