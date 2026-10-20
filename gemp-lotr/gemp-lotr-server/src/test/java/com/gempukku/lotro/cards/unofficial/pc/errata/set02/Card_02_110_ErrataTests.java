package com.gempukku.lotro.cards.unofficial.pc.errata.set02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_110_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pippin", "52_110");

					put("boromir", "1_97");       // Boromir, Son of Denethor: [gondor] Man companion who is not Aragorn, cost 3
					put("sam", "1_311");          // Sam, Son of Hamfast: Hobbit companion, cost 2
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: [gondor] Aragorn, cost 4
					put("rohanaragorn", "17_93"); // Aragorn, Defender of Rohan: [rohan] Aragorn, cost 4
					put("gandalf", "1_72");       // Gandalf, Friend of the Shirefolk: cost 4; a strength bonus only
					put("avenger", "6_27");       // Ent Avenger: [gandalf] companion who is not Gandalf, cost 4
					put("legolas", "1_50");       // Legolas, Greenleaf: an Elf, so that Elven Cloak can be played
					put("bilbo", "1_284");        // Bilbo, Retired Adventurer: a Hobbit ally, cost 2

					put("armor", "1_92");         // Armor: possession, cost 1, bearer must be a Man
					put("shield", "1_107");       // Great Shield: possession, cost 1, bearer must be a Man
					put("chain", "4_278");        // Heavy Chain: possession, cost 1, bearer must be a Man
					put("sword", "1_299");        // Hobbit Sword: possession, cost 1, bearer must be a Hobbit
					put("horn", "5_8");           // Horn of Helm: possession, cost 2, plays to the support area
					put("anduril", "17_27");      // Andúril: artifact, cost 3, bearer must be Aragorn
					put("glamdring", "1_75");     // Glamdring: possession, cost 2, bearer must be Gandalf
					put("cloak", "1_42");         // Elven Cloak: possession, cost 1, bearer must be a companion; to play, spot an Elf

					put("alliance", "1_49");      // The Last Alliance of Elves and Men: condition, cost 1, bearer must be a [gondor] Man, strength for each Elf spotted (none here); not an item
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PippinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Pippin, Mr. Took
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Companion
		 * Race: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Aragorn
		 * Game Text: Each time you play a companion, you may play an item on them from your discard pile (at twilight cost -2 if that companion is Gandalf or Aragorn).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pippin");

		assertEquals("Pippin", card.getBlueprint().getTitle());
		assertEquals("Mr. Took", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.ARAGORN, card.getBlueprint().getSignet());
	}

	@Test
	public void PippinPlayedFromHandMayPlayAnItemOnHimselfFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCardsToHand(pippin);
		scn.MoveCardsToDiscard(sword);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		scn.FreepsPlayCard(pippin);

		assertInZone(Zone.FREE_CHARACTERS, pippin);
		assertEquals(1, scn.GetTwilight());

		// Pippin is in play as his own play is answered, and he is a companion you played.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(pippin));
		scn.FreepsChooseOptionalTrigger(pippin);

		assertAttachedTo(sword, pippin);
		// 1 for Pippin + 1 for the sword
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachCompanionPlayedMayHaveAnItemFromTheDiscardPilePlayedOnHimAtFullCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var boromir = scn.GetFreepsCard("boromir");
		var sam = scn.GetFreepsCard("sam");
		var armor = scn.GetFreepsCard("armor");
		var shield = scn.GetFreepsCard("shield");
		var chain = scn.GetFreepsCard("chain");
		var sword = scn.GetFreepsCard("sword");
		var horn = scn.GetFreepsCard("horn");
		var alliance = scn.GetFreepsCard("alliance");
		var aragorn = scn.GetFreepsCard("aragorn");
		// Aragorn is in play, but he is not the companion played: the items still cost in full.
		scn.MoveCompanionsToTable(pippin, aragorn);
		scn.MoveCardsToHand(boromir, sam, chain);
		scn.MoveCardsToDiscard(armor, shield, sword, horn, alliance);

		scn.StartGame();

		scn.FreepsPlayCard(boromir);
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(pippin));
		scn.FreepsChooseOptionalTrigger(pippin);

		// Items in the discard pile Boromir can bear; not one he cannot, not one that plays to the support area,
		// not one in hand, and not a condition he could bear.
		assertTrue(scn.FreepsHasCardChoiceAvailable(armor, shield));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sword, horn, chain, alliance));
		scn.FreepsChooseCardBPFromSelection(armor);

		assertAttachedTo(armor, boromir);
		// 3 for Boromir + 1 for Armor at its full cost: Boromir is not Aragorn
		assertEquals(4, scn.GetTwilight());
		assertInDiscard(shield, sword, horn, alliance);

		// A second companion played: offered again, and declined.
		scn.FreepsPlayCard(sam);
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(pippin));
		scn.FreepsDeclineOptionalTrigger();

		assertInDiscard(shield, sword, horn, alliance);
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlayingAnAllyOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var bilbo = scn.GetFreepsCard("bilbo");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(pippin);
		scn.MoveCardsToHand(bilbo);
		scn.MoveCardsToDiscard(sword);

		scn.StartGame();

		// Bilbo is a Hobbit who could bear the sword, but an ally and not a companion.
		scn.FreepsPlayCard(bilbo);

		assertEquals(2, scn.GetTwilight());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(pippin));
		assertInDiscard(sword);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedWhenNoItemInTheDiscardPileCanGoOnTheCompanionPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var boromir = scn.GetFreepsCard("boromir");
		var sword = scn.GetFreepsCard("sword");
		var horn = scn.GetFreepsCard("horn");
		scn.MoveCompanionsToTable(pippin);
		scn.MoveCardsToHand(boromir);
		scn.MoveCardsToDiscard(sword, horn);

		scn.StartGame();

		// Playing a card from the discard pile is a cost (Q-0019, Q-0062): with nothing that can go on Boromir, no offer.
		scn.FreepsPlayCard(boromir);

		assertEquals(3, scn.GetTwilight());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(pippin));
		assertInDiscard(sword, horn);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AragornAndGandalfHaveTheirItemsPlayedAtTwilightCostMinus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var anduril = scn.GetFreepsCard("anduril");
		var glamdring = scn.GetFreepsCard("glamdring");
		scn.MoveCompanionsToTable(pippin);
		scn.MoveCardsToHand(aragorn, gandalf);
		scn.MoveCardsToDiscard(anduril, glamdring);

		scn.StartGame();

		scn.FreepsPlayCard(aragorn);
		assertEquals(4, scn.GetTwilight());
		scn.FreepsChooseOptionalTrigger(pippin);

		// Andúril is the only item Aragorn can bear, so it is chosen without asking.
		assertAttachedTo(anduril, aragorn);
		// 4 for Aragorn + 3 - 2 for Andúril
		assertEquals(5, scn.GetTwilight());

		scn.FreepsPlayCard(gandalf);
		assertEquals(9, scn.GetTwilight());
		scn.FreepsChooseOptionalTrigger(pippin);

		assertAttachedTo(glamdring, gandalf);
		// 9 + 2 - 2 for Glamdring
		assertEquals(9, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ARohanAragornHasHisItemPlayedAtTwilightCostMinus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("rohanaragorn");
		var anduril = scn.GetFreepsCard("anduril");
		scn.MoveCompanionsToTable(pippin);
		scn.MoveCardsToHand(aragorn);
		scn.MoveCardsToDiscard(anduril);

		scn.StartGame();

		scn.FreepsPlayCard(aragorn);
		assertEquals(4, scn.GetTwilight());
		scn.FreepsChooseOptionalTrigger(pippin);

		assertAttachedTo(anduril, aragorn);
		// 4 for Aragorn + 3 - 2 for Andúril
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AGandalfCultureCompanionWhoIsNotGandalfPaysFullCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var legolas = scn.GetFreepsCard("legolas");
		var avenger = scn.GetFreepsCard("avenger");
		var cloak = scn.GetFreepsCard("cloak");
		scn.MoveCompanionsToTable(pippin, legolas);
		scn.MoveCardsToHand(avenger);
		scn.MoveCardsToDiscard(cloak);

		scn.StartGame();

		scn.FreepsPlayCard(avenger);
		assertEquals(4, scn.GetTwilight());
		scn.FreepsChooseOptionalTrigger(pippin);

		assertAttachedTo(cloak, avenger);
		// 4 for Ent Avenger + 1 for Elven Cloak at its full cost
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
