package com.gempukku.lotro.cards.official.set05;

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

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_05_041_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("people", "5_41");
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: Gandalf signet
					put("boromir", "1_96");       // Boromir, Lord of Gondor: a Man with the Aragorn signet
					put("armor", "1_92");         // Armor: possession, bearer must be a Man
					put("hobbitsword", "1_299");  // Hobbit Sword: possession, bearer must be a Hobbit
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetPossessionsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("people", "5_41");
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a [gondor] Aragorn, a Man with the Gandalf signet
					// Aragorn, Defender of Rohan: the [rohan] Aragorn, no signet; his text needs the Ring-bearer assigned
					put("rohanaragorn", "17_93");
					put("boromir", "1_96");       // Boromir, Lord of Gondor: a [gondor] Man with the Aragorn signet
					put("merry", "1_303");        // Merry, From O'er the Brandywine: a [shire] Hobbit with the Aragorn signet
					put("arwen", "1_30");         // Arwen, Daughter of Elrond: an [elven] Elf with the Aragorn signet
					put("cloak", "1_42");         // Elven Cloak: an [elven] possession; to play, spot an Elf; bearer must be a companion
					put("coat", "1_101");         // Coat of Mail: a [gondor] possession, bearer must be a Man
					put("hobbitsword", "1_299");  // Hobbit Sword: a [shire] possession, bearer must be a Hobbit
					// Narsil, Blade of the Faithful: a [gondor] artifact, bearer must be a [gondor] Man
					put("narsil", "9_34");
					put("armor", "1_92");         // Armor: a [gondor] possession, put in the Shadow player's pile
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheseAreMyPeopleStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: These Are My People
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: <b>Fellowship:</b> Exert Aragorn twice to play any number of possessions from your discard pile onto companions with the Aragorn signet.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("people");

		assertEquals("These Are My People", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void IsNotPlayableUnlessAPossessionInTheDiscardPileCanBePlayedOnACompanionWithTheAragornSignet() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("people");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var armor = scn.GetFreepsCard("armor");
		var hobbitsword = scn.GetFreepsCard("hobbitsword");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveCardsToHand(card);
		//A possession that could be played, but only on Frodo, who has the Frodo signet
		scn.MoveCardsToDiscard(hobbitsword);

		scn.StartGame();

		//Playing from the discard pile is treated as a cost: with nothing to play, the event is withheld
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(card));

		//Armor can go on Boromir, who has the Aragorn signet
		scn.MoveCardsToDiscard(armor);
		scn.FreepsRefreshDecision();

		assertTrue(scn.FreepsPlayAvailable(card));
	}

	@Test
	public void ExertsAragornTwiceToPlayPossessionsFromTheDiscardPileOntoCompanionsWithTheAragornSignet() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPossessionsScenario();

		var people = scn.GetFreepsCard("people");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var merry = scn.GetFreepsCard("merry");
		var arwen = scn.GetFreepsCard("arwen");
		var cloak = scn.GetFreepsCard("cloak");
		var coat = scn.GetFreepsCard("coat");
		var hobbitsword = scn.GetFreepsCard("hobbitsword");
		var narsil = scn.GetFreepsCard("narsil");
		var othersArmor = scn.GetShadowCard("armor");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, boromir, merry, arwen);
		scn.MoveCardsToHand(people);
		scn.MoveCardsToDiscard(cloak, coat, hobbitsword, narsil);
		scn.MoveCardsToDiscard(othersArmor);

		scn.StartGame();
		assertInHand(people);
		assertInDiscard(cloak, coat, hobbitsword, narsil, othersArmor);
		assertEquals(0, scn.GetWoundsOn(aragorn));

		assertTrue(scn.FreepsPlayAvailable(people));
		scn.FreepsPlayCard(people);
		// Aragorn exerts twice.
		assertEquals(2, scn.GetWoundsOn(aragorn));

		// Possessions of three cultures; not an artifact, nor a possession in the other player's pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(cloak, coat, hobbitsword));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(narsil, othersArmor));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(cloak);
		// Any companion with the Aragorn signet, of three cultures and races; not Aragorn, a Man with the Gandalf
		// signet, nor Frodo, a Hobbit with the Frodo signet, though the Cloak's own text would take either.
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, merry, arwen));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(merry);
		assertAttachedTo(cloak, merry);

		// A second possession: the Coat of Mail goes on Boromir, the one Man with the Aragorn signet, unasked.
		assertTrue(scn.FreepsHasCardChoiceAvailable(coat, hobbitsword));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(coat);
		assertAttachedTo(coat, boromir);

		// The Hobbit Sword could still go on Merry; the player stops.
		assertTrue(scn.FreepsHasCardChoiceAvailable(hobbitsword));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsDeclineChoosing();

		assertAttachedTo(cloak, merry);
		assertAttachedTo(coat, boromir);
		assertInDiscard(hobbitsword, narsil, othersArmor);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertInDiscard(people);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AragornDefenderOfRohanMayPayAndNoPossessionNeedBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPossessionsScenario();

		var people = scn.GetFreepsCard("people");
		var rohanaragorn = scn.GetFreepsCard("rohanaragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var coat = scn.GetFreepsCard("coat");
		scn.MoveCompanionsToTable(rohanaragorn, boromir);
		scn.MoveCardsToHand(people);
		scn.MoveCardsToDiscard(coat);

		scn.StartGame();
		assertInHand(people);
		assertInDiscard(coat);
		assertEquals(0, scn.GetWoundsOn(rohanaragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));

		assertTrue(scn.FreepsPlayAvailable(people));
		scn.FreepsPlayCard(people);
		// The [rohan] Aragorn exerts twice; Boromir, who is no Aragorn, does not.
		assertEquals(2, scn.GetWoundsOn(rohanaragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));

		// Any number of possessions includes none.
		assertTrue(scn.FreepsHasCardChoiceAvailable(coat));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsDeclineChoosing();

		assertInDiscard(coat);
		assertNotAttachedTo(coat, boromir);
		assertInDiscard(people);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotPlayableWhileAragornCannotExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPossessionsScenario();

		var people = scn.GetFreepsCard("people");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var coat = scn.GetFreepsCard("coat");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveCardsToHand(people);
		scn.MoveCardsToDiscard(coat);
		// Vitality 4 with 2 wounds: Aragorn can exert once, not twice. Boromir, who could, is no Aragorn.
		scn.AddWoundsToChar(aragorn, 2);

		scn.StartGame();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertInDiscard(coat);
		assertInHand(people);

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(people));
	}
}
