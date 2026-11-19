package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_058_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer; the Shadow phase of
	// turn 1 is at The Troll Hoard (site 2), whose only text is a start-of-turn offer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("servants", "32_58");

					// Wise characters, and characters who are not Wise
					put("gandalf", "30_26");    // Gandalf, Friend of Thorin: Wise [gandalf] companion; his other texts want [dwarven] followers or Thorin in hand
					put("elrond", "30_21");     // Elrond, Elven Lord: Wise [elven] ally; a start-of-turn heal, a regroup ability not used
					put("thorin", "30_19");     // Thorin, Oakenshield: [dwarven] companion, not Wise; a fellowship ability not used
					put("lindir", "33_14");     // Lindir, Elven Minstrel: [elven] Elf ally, not Wise; a heal as the fellowship moves

					// [wraith] minions of two races, a [wraith] card that is no minion, a minion of another culture
					put("nelya", "32_63");      // Úlairë Nelya, Revived: [wraith] Nazgûl, strength 10, cost 5, site 5; damage +1 only while a companion bears a follower
					put("hound", "7_192");      // Morgul Hound: [wraith] Orc, strength 5, cost 2; +4 and fierce only at 2 wounds on the Ring-bearer
					put("mongrel", "7_194");    // Morgul Mongrel: [wraith] Orc, cost 3; fierce and damage +1 only at 3 wounds on the Ring-bearer
					put("blade", "32_56");      // Dol Guldur Blade: [wraith] possession, hand weapon +2, cost 0, on a [wraith] minion; its maneuver ability not used
					put("runner", "31_30");     // Goblin Runner: [moria] Orc, strength 5, cost 1; a text on its own play only
					put("underling", "33_50");  // Orc Underling: [sauron] Orc, cost 1; a response to an Orc's wound not used
					// Orcs for the hand, and a minion that is no Orc
					put("sentinel", "33_49");   // Orc Sentinel: [sauron] Orc; its shadow ability wants another [sauron] Orc in play
					put("lemenya", "32_62");    // Úlairë Lemenya, Revived: [wraith] Nazgûl; texts that work only in play

					// The Decipher [wraith] Orcs of the file's earlier test
					put("brute", "7_188");      // Morgul Brute: [wraith] Orc; its play's burden wants a Nazgûl, and it is never played
					put("lackey", "7_193");     // Morgul Lackey: [wraith] Orc, cost 2, site 4; a skirmish ability
					put("whelp", "7_202");      // Morgul Whelp: [wraith] Orc, cost 1, site 4; strength +6 while the Ring-bearer is wounded
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void HeIsSummoningHisServantsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: He Is Summoning His Servants
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: If you cannot spot 2 Wise characters, each [wraith] minion is strength +2.
		 * <b>Shadow:</b> Discard an Orc from hand to play a [wraith] minion from your discard pile.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("servants");

		assertEquals("He Is Summoning His Servants", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachWraithMinionIsStrengthPlusTwoUntilASecondWiseCharacterCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var servants = scn.GetShadowCard("servants");
		var nelya = scn.GetShadowCard("nelya");
		var hound = scn.GetShadowCard("hound");
		var blade = scn.GetShadowCard("blade");
		var runner = scn.GetShadowCard("runner");
		var gandalf = scn.GetFreepsCard("gandalf");
		var elrond = scn.GetFreepsCard("elrond");
		var thorin = scn.GetFreepsCard("thorin");
		var lindir = scn.GetFreepsCard("lindir");
		scn.MoveCompanionsToTable(gandalf, thorin);
		scn.MoveCardsToHand(elrond);

		scn.StartGame();
		scn.MoveAlliesToTable(lindir);
		scn.MoveCardsToSupportArea(servants);
		// Minions cheated in during the fellowship phase, only to read the standing text on them (Q-0228).
		scn.MoveMinionsToTable(nelya, hound, runner);
		scn.AttachCardsTo(hound, blade);
		assertInZone(Zone.SUPPORT, servants);
		assertInZone(Zone.SUPPORT, lindir);
		assertAttachedTo(blade, hound);
		assertInHand(elrond);
		scn.FreepsRefreshDecision();

		// One Wise character, Gandalf; neither Thorin nor Lindir is Wise.
		assertEquals(12, scn.GetStrength(nelya));   // 10 + 2
		assertEquals(9, scn.GetStrength(hound));    // 5 + 2 Blade + 2
		assertEquals(5, scn.GetStrength(runner));   // a [moria] Orc

		// A second Wise character, an ally this time.
		scn.FreepsPlayCard(elrond);
		assertInZone(Zone.SUPPORT, elrond);
		assertEquals(10, scn.GetStrength(nelya));
		assertEquals(7, scn.GetStrength(hound));
		assertEquals(5, scn.GetStrength(runner));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ShadowAbilityDiscardsAnOrcFromHandToPlayAWraithMinionFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var servants = scn.GetShadowCard("servants");
		var sentinel = scn.GetShadowCard("sentinel");
		var runner = scn.GetShadowCard("runner");
		var lemenya = scn.GetShadowCard("lemenya");
		var nelya = scn.GetShadowCard("nelya");
		var hound = scn.GetShadowCard("hound");
		var blade = scn.GetShadowCard("blade");
		var underling = scn.GetShadowCard("underling");
		var mongrel = scn.GetShadowCard("mongrel");
		// The Free Peoples player's own Mongrel, in his own discard pile: a title the Shadow player's piles do not hold
		var fpMongrel = scn.GetFreepsCard("mongrel");
		scn.MoveCardsToHand(sentinel, runner, lemenya);
		scn.MoveCardsToDiscard(nelya, hound, blade, underling, fpMongrel);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.MoveCardsToSupportArea(servants);
		// The Mongrel is there so that the Blade has a [wraith] minion to go on, and could be paid for.
		scn.MoveMinionsToTable(mongrel);
		assertInZone(Zone.SUPPORT, servants);
		assertInHand(sentinel, runner, lemenya);
		assertInDiscard(nelya, hound, blade, underling, fpMongrel);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(servants));
		scn.ShadowUseCardAction(servants);
		// Orcs of two cultures; not the Nazgûl.
		assertTrue(scn.ShadowHasCardChoiceAvailable(sentinel, runner));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(lemenya));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(runner);
		assertInDiscard(runner);
		assertInHand(sentinel, lemenya);

		// [wraith] minions of two races; not the [wraith] possession, nor the [sauron] Orc, nor the other player's card.
		assertTrue(scn.ShadowHasCardChoiceAvailable(nelya, hound));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(blade, underling, fpMongrel));
		scn.ShadowChooseCardBPFromSelection(nelya);
		assertInZone(Zone.SHADOW_CHARACTERS, nelya);
		assertInDiscard(hound, blade, underling, fpMongrel);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheMinionPlayedIsNotTheOrcDiscardedToPayForIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var servants = scn.GetShadowCard("servants");
		var brute = scn.GetShadowCard("brute");
		var lackey = scn.GetShadowCard("lackey");
		var whelp = scn.GetShadowCard("whelp");

		scn.MoveCardsToSupportArea(servants);
		scn.MoveCardsToHand(brute);
		scn.MoveCardsToDiscard(lackey, whelp);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, servants);
		assertInHand(brute);
		assertInDiscard(lackey, whelp);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(servants));
		scn.ShadowUseCardAction(servants);
		// The one Orc in hand pays the cost without a choice
		assertEquals(0, scn.GetShadowHandCount());
		assertInDiscard(brute);

		// The two [wraith] minions that lay in the discard pile before the cost was paid, and not the Morgul Brute that
		// lies there now because it paid for it (CR 4.2 "effect", the They Are Coming example; Q-0320; F-0545).
		assertTrue(scn.ShadowHasCardChoiceAvailable(lackey, whelp));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(brute));
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		scn.ShadowChooseCardBPFromSelection(whelp);

		assertInZone(Zone.SHADOW_CHARACTERS, whelp);
		assertInDiscard(lackey, brute);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedWithNoOrcInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var servants = scn.GetShadowCard("servants");
		var lemenya = scn.GetShadowCard("lemenya");
		var nelya = scn.GetShadowCard("nelya");
		scn.MoveCardsToHand(lemenya);
		scn.MoveCardsToDiscard(nelya);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.MoveCardsToSupportArea(servants);
		assertInZone(Zone.SUPPORT, servants);
		assertInHand(lemenya);
		assertInDiscard(nelya);
		scn.FreepsPass();

		// A Nazgûl in hand is no Orc.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(servants));
	}

	@Test
	public void ShadowAbilityIsNotOfferedUntilAWraithMinionInTheDiscardPileCanBePaidFor() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var servants = scn.GetShadowCard("servants");
		var sentinel = scn.GetShadowCard("sentinel");
		var nelya = scn.GetShadowCard("nelya");
		var blade = scn.GetShadowCard("blade");
		var underling = scn.GetShadowCard("underling");
		var mongrel = scn.GetShadowCard("mongrel");
		scn.MoveCardsToHand(sentinel);
		scn.MoveCardsToDiscard(nelya, blade, underling);

		scn.StartGame();
		scn.MoveCardsToSupportArea(servants);
		scn.MoveMinionsToTable(mongrel);
		assertInZone(Zone.SUPPORT, servants);
		assertInHand(sentinel);
		assertInDiscard(nelya, blade, underling);
		scn.FreepsPass();

		// Nelya costs 5 + 2 roaming (a site 5 minion at site 2).  The Blade (0) and the Underling (1 + 2 roaming)
		// could be paid for, and are no [wraith] minions.
		scn.SetTwilight(6);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(servants));

		scn.SetTwilight(7);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(servants));
		assertInHand(sentinel);
		assertInDiscard(nelya, blade, underling);
	}
}
